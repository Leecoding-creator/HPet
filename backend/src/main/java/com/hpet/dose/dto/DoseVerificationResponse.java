package com.hpet.dose.dto;

public class DoseVerificationResponse {
    private final boolean verified;
    private final String reason;
    private final int verifiedCountToday;    // 오늘 사진 인증 성공한 서로 다른 영양제 개수
    private final int requiredCountToday;    // 오늘 하루치 완성에 필요한 영양제 개수 (= 등록 개수 n)
    private final boolean dayCompleted;      // 이번 인증으로 오늘 하루치(포션 10점)가 완성됐는지
    private final int growthPointsAfter;     // 인증 처리 후 캐릭터의 누적 경험치(0~300)
    private final String imageUrl;           // 저장된 인증 사진 URL (인증 성공 시에만 값 있음)

    // 2차 기획안 반영: 캐릭터 모션(포션 섭취 애니메이션)에서 "오늘 획득 성장치 +N/10" 표시용
    private final int pointsGainedThisTime;  // 이번 인증으로 실제 늘어난 경험치 (이미 오늘 다 채웠으면 0)
    private final int todayEarnedPoints;     // 오늘 누적 획득 경험치 (0~10)
    private final int dailyMaxPoints;        // 하루 최대 경험치 (항상 10) - 프론트에서 "N/10" 표시할 때 분모로 씀

    public DoseVerificationResponse(boolean verified, String reason, int verifiedCountToday,
                                     int requiredCountToday, boolean dayCompleted, int growthPointsAfter,
                                     String imageUrl, int pointsGainedThisTime, int todayEarnedPoints,
                                     int dailyMaxPoints) {
        this.verified = verified;
        this.reason = reason;
        this.verifiedCountToday = verifiedCountToday;
        this.requiredCountToday = requiredCountToday;
        this.dayCompleted = dayCompleted;
        this.growthPointsAfter = growthPointsAfter;
        this.imageUrl = imageUrl;
        this.pointsGainedThisTime = pointsGainedThisTime;
        this.todayEarnedPoints = todayEarnedPoints;
        this.dailyMaxPoints = dailyMaxPoints;
    }

    public boolean isVerified() { return verified; }
    public String getReason() { return reason; }
    public int getVerifiedCountToday() { return verifiedCountToday; }
    public int getRequiredCountToday() { return requiredCountToday; }
    public boolean isDayCompleted() { return dayCompleted; }
    public int getGrowthPointsAfter() { return growthPointsAfter; }
    public String getImageUrl() { return imageUrl; }
    public int getPointsGainedThisTime() { return pointsGainedThisTime; }
    public int getTodayEarnedPoints() { return todayEarnedPoints; }
    public int getDailyMaxPoints() { return dailyMaxPoints; }
}
