package com.hpet.profile.dto;

public class RecommendationResponse {
    private final Long supplementId;
    private final String supplementName;
    private final String reason; // 왜 추천됐는지 (규칙 기반이라 근거를 그대로 보여줄 수 있음)

    public RecommendationResponse(Long supplementId, String supplementName, String reason) {
        this.supplementId = supplementId;
        this.supplementName = supplementName;
        this.reason = reason;
    }

    public Long getSupplementId() { return supplementId; }
    public String getSupplementName() { return supplementName; }
    public String getReason() { return reason; }
}
