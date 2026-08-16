package com.hpet.vision;

/**
 * Phase 5 - 5-4. AI 비전 판정 로직을 공급사(OpenAI/Gemini/Groq 등)로부터 분리하는 인터페이스.
 * 회의 1-5 확정: 지금은 OpenAI만 구현하지만, 나중에 다른 공급사로 바꿀 일이 생겨도
 * 이 인터페이스를 구현하는 클래스만 새로 만들면 되고 서비스 로직(DoseVerificationService)은
 * 그대로 재사용된다.
 */
public interface AiVisionClient {

    /**
     * @param imageBytes     인증 사진 원본 바이트
     * @param mimeType       예: "image/jpeg", "image/png"
     * @param supplementName 인증 대상 영양제 이름 (예: "비타민") - 프롬프트에 활용
     */
    VisionJudgement judge(byte[] imageBytes, String mimeType, String supplementName);

    /**
     * 자세(거북목) 판정용. success=true면 "거북목/자세 불량이 감지됨"을 의미한다
     * (영양제 인증의 success=true "인증 성공"과 의미가 반대이니 호출부에서 헷갈리지 않게 주의).
     */
    VisionJudgement judgePosture(byte[] imageBytes, String mimeType);
}
