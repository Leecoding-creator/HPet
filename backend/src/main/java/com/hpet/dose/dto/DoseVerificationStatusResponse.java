package com.hpet.dose.dto;

import java.time.LocalDateTime;

public class DoseVerificationStatusResponse {
    private final int pillCount;       // 오늘 인증된 알약 개수 (아직 인증 안 했으면 0)
    private final int requiredCount;   // 오늘 필요한 개수 (= 등록 영양제 개수 n)
    private final String imageUrl;     // 오늘 가장 최근 인증 사진 (없으면 null)
    private final LocalDateTime verifiedAt;

    public DoseVerificationStatusResponse(int pillCount, int requiredCount, String imageUrl, LocalDateTime verifiedAt) {
        this.pillCount = pillCount;
        this.requiredCount = requiredCount;
        this.imageUrl = imageUrl;
        this.verifiedAt = verifiedAt;
    }

    public int getPillCount() { return pillCount; }
    public int getRequiredCount() { return requiredCount; }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
}
