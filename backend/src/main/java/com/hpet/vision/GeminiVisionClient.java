package com.hpet.vision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Gemini(gemini-3.1-flash-lite) 기반 AI 비전 판정.
 * AiVisionClient 인터페이스 덕분에 서비스 로직은 그대로 두고 공급사만 교체한다.
 *
 * ai.provider=gemini(기본값)일 때 활성화되며, 환경변수 GEMINI_API_KEY가 필요하다.
 * 키가 없으면 서버가 죽지 않도록 명확한 실패 사유를 반환한다(예외를 던지지 않음).
 */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiVisionClient implements AiVisionClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionClient.class);
    private static final int MAX_ATTEMPTS = 2; // 최초 시도 + 재시도 1회

    private final String apiKey;
    private final String model;
    private final String endpointBase;
    private final int maxImageDimension;
    private final float imageQuality;
    private final ImageResizer imageResizer;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiVisionClient(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-3.1-flash-lite}") String model,
            @Value("${gemini.endpoint-base:https://generativelanguage.googleapis.com/v1beta/models}") String endpointBase,
            @Value("${gemini.max-image-dimension:1024}") int maxImageDimension,
            @Value("${gemini.image-quality:0.85}") float imageQuality,
            ImageResizer imageResizer) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpointBase = endpointBase;
        this.maxImageDimension = maxImageDimension;
        this.imageQuality = imageQuality;
        this.imageResizer = imageResizer;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public VisionJudgement judgePillCount(byte[] imageBytes, String mimeType) {
        // 팀 확정(2026-08-19): 영양제 종류는 구별하지 않고 "몇 개 보이는지"만 센다.
        String prompt = "이 사진에서 사용자가 알약(영양제/보충제)들을 손에 들고 있거나, 입에 넣거나, "
                + "복용 직전 준비된 모습인지 확인하고, 보이는 알약의 개수를 정확히 세어줘. "
                + "무슨 영양제인지(색깔/모양으로 성분을 맞히는 것)는 판단하지 않아도 되고, 개수만 정확히 세는 게 중요해. "
                + "알약이 하나도 안 보이면 success는 false, 1개 이상 보이면 true로 해줘. "
                + "반드시 아래 JSON 형식으로만 답해. 다른 텍스트는 절대 포함하지 마: "
                + "{\"success\": true 또는 false, \"pillCount\": 보이는 알약 개수(정수), "
                + "\"reason\": \"판단 근거를 한국어로 한 문장 (예: 알약 2개를 손에 들고 있음)\"}";
        return callVisionApi(imageBytes, mimeType, prompt);
    }

    @Override
    public VisionJudgement judgePosture(byte[] imageBytes, String mimeType) {
        // success=true가 "거북목/자세 불량이 감지됨"을 의미한다.
        String prompt = "이 사진 속 사람이 거북목 자세(고개가 앞으로 많이 나와있고 어깨가 굽은 상태)인지 판정해줘. "
                + "반드시 아래 JSON 형식으로만 답해. 다른 텍스트는 절대 포함하지 마: "
                + "{\"success\": true 또는 false (true면 거북목/자세 불량이 감지된 것), "
                + "\"reason\": \"판단 근거를 한국어로 한 문장\"}";
        return callVisionApi(imageBytes, mimeType, prompt);
    }

    private VisionJudgement callVisionApi(byte[] imageBytes, String mimeType, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY가 설정되지 않아 AI 판정을 건너뜁니다.");
            return new VisionJudgement(false, "Gemini API 키가 설정되지 않았습니다. 환경변수 GEMINI_API_KEY를 확인해주세요.", 0);
        }

        // 원본을 그대로 보내면 토큰(비용)이 많이 나가서, API 호출 직전에 축소한다.
        ImageResizer.Resized resized = imageResizer.resize(imageBytes, maxImageDimension, imageQuality);
        String requestBody = buildRequestBody(resized.bytes(), resized.mimeType(), prompt);
        String url = endpointBase + "/" + model + ":generateContent";

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return parseJudgement(response.body());
                }

                log.warn("Gemini Vision API 응답 실패: status={}, attempt={}, body={}",
                        response.statusCode(), attempt, truncate(response.body()));

                // 429(rate limit), 5xx는 재시도 가치가 있고 그 외 4xx(키 오류 등)는 재시도해도 소용없음
                if (response.statusCode() < 500 && response.statusCode() != 429) {
                    return new VisionJudgement(false, "AI 판정 요청이 거부되었습니다 (status=" + response.statusCode() + ")", 0);
                }
            } catch (Exception e) {
                log.warn("Gemini Vision API 호출 중 오류 (attempt={}): {}", attempt, e.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    return new VisionJudgement(false, "AI 판정 서버 호출에 실패했습니다: " + e.getMessage(), 0);
                }
            }
        }
        return new VisionJudgement(false, "AI 판정에 실패했습니다. 잠시 후 다시 시도해주세요.", 0);
    }

    /**
     * Gemini generateContent 요청 바디.
     * { contents:[{ parts:[{text}, {inline_data:{mime_type, data}}] }],
     *   generationConfig:{ responseMimeType:"application/json" } }
     */
    private String buildRequestBody(byte[] imageBytes, String mimeType, String prompt) {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        content.put("role", "user");
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);
        ObjectNode inline = parts.addObject().putObject("inline_data");
        inline.put("mime_type", mimeType);
        inline.put("data", Base64.getEncoder().encodeToString(imageBytes));

        root.putObject("generationConfig").put("responseMimeType", "application/json");

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("요청 바디 직렬화 실패", e);
        }
    }

    private VisionJudgement parseJudgement(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");

            // 생각(thought) 파트는 건너뛰고 실제 답변 텍스트만 꺼낸다.
            String text = null;
            for (JsonNode part : parts) {
                if (part.path("thought").asBoolean(false)) continue;
                if (part.hasNonNull("text")) {
                    text = part.get("text").asText();
                    break;
                }
            }
            if (text == null || text.isBlank()) {
                log.warn("Gemini 응답에 텍스트가 없음: {}", truncate(responseBody));
                return new VisionJudgement(false, "AI 응답을 해석하지 못했습니다.", 0);
            }

            JsonNode judgementNode = objectMapper.readTree(stripCodeFence(text));
            boolean success = judgementNode.path("success").asBoolean(false);
            String reason = judgementNode.path("reason").asText("판정 사유가 제공되지 않았습니다.");
            int pillCount = judgementNode.path("pillCount").asInt(0);
            return new VisionJudgement(success, reason, pillCount);
        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패: {}", e.getMessage());
            return new VisionJudgement(false, "AI 응답을 해석하지 못했습니다.", 0);
        }
    }

    /** 혹시 ```json ... ``` 으로 감싸서 오는 경우 대비 */
    private String stripCodeFence(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            int lastFence = t.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return t.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return t;
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}
