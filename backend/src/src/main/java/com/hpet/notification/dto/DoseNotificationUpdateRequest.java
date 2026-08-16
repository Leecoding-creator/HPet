package com.hpet.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class DoseNotificationUpdateRequest {

    @NotNull(message = "알림 시간은 필수입니다.")
    private LocalTime notifyTime;

    private boolean enabled = true;

    public LocalTime getNotifyTime() { return notifyTime; }
    public void setNotifyTime(LocalTime notifyTime) { this.notifyTime = notifyTime; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
