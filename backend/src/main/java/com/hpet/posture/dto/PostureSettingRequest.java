package com.hpet.posture.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class PostureSettingRequest {

    @NotNull(message = "enabled는 필수입니다.")
    private Boolean enabled;

    private LocalTime startTime;

    private LocalTime endTime;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
