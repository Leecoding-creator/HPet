package com.hpet.vision;

/**
 * Phase 5 - 5-4. AI 비전 판정 로직을 공급사(OpenAI/Gemini/Groq 등)로부터 분리하는 인터페이스.
 * 회의 1-5 확정: 지금은 OpenAI만 구현하지만, 나중에 다른 공급사로 바꿀 일이 생겨도
 * 이 인터페이스를 구현하는 클래스만 새로 만들면 되고 서비스 로직은 그대로 재사용된다.
 */
public interface AiVisionClient {

    /**
     * 팀 확정(2026-08-19, 준호님): "영양제별로 따로 안 하고 통째로(전체) 인증"으로 확정.
     * 알약의 색으로 대강 짐작은 가능해도 정확히 무슨 영양제인지 AI가 구별할 수는 없으므로,
     * 구별하려 하지 않고 "손/입 근처에 알약이 몇 개 보이는지"만 센다. 오늘 등록한 영양제
     * 개수(n)만큼 알약을 모아서 찍으면, 그 개수를 세어서 몇 개를 인증했는지 판단한다.
     */
    VisionJudgement judgePillCount(byte[] imageBytes, String mimeType);

    /**
     * 자세(거북목) 판정용. success=true면 "거북목/자세 불량이 감지됨"을 의미한다.
     */
    VisionJudgement judgePosture(byte[] imageBytes, String mimeType);
}
