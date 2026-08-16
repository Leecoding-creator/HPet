package com.hpet.dose.dto;

public class DoseVerificationResponse {
    private final boolean verified;
    private final String reason;
    private final int verifiedCountToday;   // 오늘 사진 인증 성공한 서로 다른 영양제 개수
    private final int requiredCountToday;   // 오늘 하루치 완성에 필요한 영양제 개수 (= 등록 개수 n)
    private final boolean dayCompleted;     // 이번 인증으로 오늘 하루치(포션 10점)가 완성됐는지
    private final int growthDaysAfter;      // 인증 처리 후 캐릭터의 누적 성장 일수
    private final String imageUrl;          // 저장된 인증 사진 URL (인증 성공 시에만 값 있음)

    public DoseVerificationResponse(boolean verified, String reason, int verifiedCountToday,
                                     int requiredCountToday, boolean dayCompleted, int growthDaysAfter,
                                     String imageUrl) {
        this.verified = verified;
        this.reason = reason;
        this.verifiedCountToday = verifiedCountToday;
        this.requiredCountToday = requiredCountToday;
        this.dayCompleted = dayCompleted;
        this.growthDaysAfter = growthDaysAfter;
        this.imageUrl = imageUrl;
    }

    public boolean isVerified() { return verified; }
    public String getReason() { return reason; }
    public int getVerifiedCountToday() { return verifiedCountToday; }
    public int getRequiredCountToday() { return requiredCountToday; }
    public boolean isDayCompleted() { return dayCompleted; }
    public int getGrowthDaysAfter() { return growthDaysAfter; }
    public String getImageUrl() { return imageUrl; }
}
