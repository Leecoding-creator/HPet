package com.hpet.notification.dto;

import java.time.LocalTime;

public class DoseNotificationResponse {
    private final Long id;
    private final Long userSupplementId;
    private final String supplementName;
    private final LocalTime notifyTime;
    private final boolean enabled;

    public DoseNotificationResponse(Long id, Long userSupplementId, String supplementName,
                                     LocalTime notifyTime, boolean enabled) {
        this.id = id;
        this.userSupplementId = userSupplementId;
        this.supplementName = supplementName;
        this.notifyTime = notifyTime;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public Long getUserSupplementId() { return userSupplementId; }
    public String getSupplementName() { return supplementName; }
    public LocalTime getNotifyTime() { return notifyTime; }
    public boolean isEnabled() { return enabled; }
}
