package com.hpet.posture.dto;

public class PostureVerificationResponse {
    private final boolean turtleNeckDetected; // true면 거북목/자세 불량이 감지된 것
    private final String reason;
    private final String imageUrl; // 감지된 경우에만 값 있음

    public PostureVerificationResponse(boolean turtleNeckDetected, String reason, String imageUrl) {
        this.turtleNeckDetected = turtleNeckDetected;
        this.reason = reason;
        this.imageUrl = imageUrl;
    }

    public boolean isTurtleNeckDetected() { return turtleNeckDetected; }
    public String getReason() { return reason; }
    public String getImageUrl() { return imageUrl; }
}
