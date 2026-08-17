package com.hpet.vision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Phase 5 - 5-5. 회의 1-5 확정: OpenAI로 바로 채택 (Gemini/Groq 구현 없음).
 * 4장 문서(API 테스트 계획)에 따라 JSON mode로 강하게 지시해서 파싱 실패율을 낮춘다.
 *
 * ⚠️ 실제 호출을 위해서는 환경변수 OPENAI_API_KEY가 설정돼 있어야 한다.
 * 키가 없으면 개발/데모 중 서버가 죽지 않도록 명확한 실패 사유를 반환한다(예외를 던지지 않음).
 */
@Component
public class OpenAiVisionClient implements AiVisionClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiVisionClient.class);
    private static final int MAX_ATTEMPTS = 2; // 최초 시도 + 재시도 1회

    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final int maxImageDimension;
    private final float imageQuality;
    private final ImageResizer imageResizer;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiVisionClient(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.vision-endpoint}") String endpoint,
            @Value("${openai.max-image-dimension:1024}") int maxImageDimension,
            @Value("${openai.image-quality:0.85}") float imageQuality,
            ImageResizer imageResizer) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
        this.maxImageDimension = maxImageDimension;
        this.imageQuality = imageQuality;
        this.imageResizer = imageResizer;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public VisionJudgement judge(byte[] imageBytes, String mimeType, String supplementName) {
        String prompt = "이 사진이 사용자가 '" + supplementName + "'(영양제/보충제)를 실제로 복용하고 있거나 "
                + "복용 직전 준비된 모습(예: 손이나 입 근처에 해당 영양제가 보이는 상태)을 보여주는지 판정해줘. "
                + "반드시 아래 JSON 형식으로만 답해. 다른 텍스트는 절대 포함하지 마: "
                + "{\"success\": true 또는 false, \"reason\": \"판단 근거를 한국어로 한 문장\"}";
        return callVisionApi(imageBytes, mimeType, prompt);
    }

    @Override
    public VisionJudgement judgePosture(byte[] imageBytes, String mimeType) {
        // 여기서는 success=true가 "거북목/자세 불량이 감지됨"을 의미하도록 프롬프트를 짠다.
        String prompt = "이 사진 속 사람이 거북목 자세(고개가 앞으로 많이 나와있고 어깨가 굽은 상태)인지 판정해줘. "
                + "반드시 아래 JSON 형식으로만 답해. 다른 텍스트는 절대 포함하지 마: "
                + "{\"success\": true 또는 false (true면 거북목/자세 불량이 감지된 것), "
                + "\"reason\": \"판단 근거를 한국어로 한 문장\"}";
        return callVisionApi(imageBytes, mimeType, prompt);
    }

    private VisionJudgement callVisionApi(byte[] imageBytes, String mimeType, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENAI_API_KEY가 설정되지 않아 AI 판정을 건너뜁니다.");
            return new VisionJudgement(false, "OpenAI API 키가 설정되지 않았습니다. 환경변수 OPENAI_API_KEY를 확인해주세요.");
        }

        // 팀 피드백 반영: 원본을 그대로 보내면 토큰(비용)이 많이 나가서, API 호출 직전에 축소한다.
        // 판정 정확도에 지장 없는 선(기본 1024px)까지만 줄이고, 이미 작으면 확대하지 않는다.
        ImageResizer.Resized resized = imageResizer.resize(imageBytes, maxImageDimension, imageQuality);

        String requestBody = buildRequestBody(resized.bytes(), resized.mimeType(), prompt);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(20))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return parseJudgement(response.body());
                }

                log.warn("OpenAI Vision API 응답 실패: status={}, attempt={}, body={}",
                        response.statusCode(), attempt, truncate(response.body()));

                // 429(rate limit), 5xx는 재시도 가치가 있고 4xx(키 오류 등)는 재시도해도 소용없음
                if (response.statusCode() < 500 && response.statusCode() != 429) {
                    return new VisionJudgement(false, "AI 판정 요청이 거부되었습니다 (status=" + response.statusCode() + ")");
                }
            } catch (Exception e) {
                log.warn("OpenAI Vision API 호출 중 오류 (attempt={}): {}", attempt, e.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    return new VisionJudgement(false, "AI 판정 서버 호출에 실패했습니다: " + e.getMessage());
                }
            }
        }
        return new VisionJudgement(false, "AI 판정에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    private String buildRequestBody(byte[] imageBytes, String mimeType, String prompt) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.putObject("response_format").put("type", "json_object");

        ArrayNode messages = root.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");

        ArrayNode content = userMessage.putArray("content");
        content.addObject().put("type", "text").put("text", prompt);

        ObjectNode imagePart = content.addObject();
        imagePart.put("type", "image_url");
        imagePart.putObject("image_url").put("url", dataUrl);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("요청 바디 직렬화 실패", e);
        }
    }

    private VisionJudgement parseJudgement(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            JsonNode judgementNode = objectMapper.readTree(content);
            boolean success = judgementNode.path("success").asBoolean(false);
            String reason = judgementNode.path("reason").asText("판정 사유가 제공되지 않았습니다.");
            return new VisionJudgement(success, reason);
        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패: {}", e.getMessage());
            return new VisionJudgement(false, "AI 응답을 해석하지 못했습니다.");
        }
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}
