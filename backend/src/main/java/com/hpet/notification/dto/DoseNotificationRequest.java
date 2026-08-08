package com.hpet.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class DoseNotificationRequest {

    @NotNull(message = "userSupplementId는 필수입니다.")
    private Long userSupplementId;

    @NotNull(message = "알림 시간은 필수입니다.")
    private LocalTime notifyTime;

    private boolean enabled = true;

    public Long getUserSupplementId() { return userSupplementId; }
    public void setUserSupplementId(Long userSupplementId) { this.userSupplementId = userSupplementId; }

    public LocalTime getNotifyTime() { return notifyTime; }
    public void setNotifyTime(LocalTime notifyTime) { this.notifyTime = notifyTime; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
