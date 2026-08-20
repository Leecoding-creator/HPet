package com.hpet.posture.dto;

import java.time.LocalDateTime;

public class PostureEventResponse {
    private final Long id;
    private final LocalDateTime detectedAt;
    private final Integer angleDeg;
    private final Integer durationMin;
    private final LocalDateTime createdAt;

    public PostureEventResponse(Long id, LocalDateTime detectedAt, Integer angleDeg, Integer durationMin,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.detectedAt = detectedAt;
        this.angleDeg = angleDeg;
        this.durationMin = durationMin;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public Integer getAngleDeg() { return angleDeg; }
    public Integer getDurationMin() { return durationMin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
