package com.hpet.posture.dto;

import java.time.LocalTime;

public class PostureSettingResponse {
    private final boolean enabled;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public PostureSettingResponse(boolean enabled, LocalTime startTime, LocalTime endTime) {
        this.enabled = enabled;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isEnabled() { return enabled; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
