package com.hpet.posture.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class PostureEventCreateRequest {

    @NotNull(message = "detectedAt은 필수입니다.")
    private LocalDateTime detectedAt;

    @NotNull(message = "angleDeg는 필수입니다.")
    private Integer angleDeg;

    @NotNull(message = "durationMin은 필수입니다.")
    private Integer durationMin;

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public Integer getAngleDeg() { return angleDeg; }
    public void setAngleDeg(Integer angleDeg) { this.angleDeg = angleDeg; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }
}
