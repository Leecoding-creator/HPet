package com.hpet.supplement.dto;

import java.time.LocalDateTime;

public class UserSupplementResponse {
    private final Long userSupplementId;
    private final Long supplementId; // null if no master supplement mapped
    private final String customName;
    private final String doseTime;
    private final LocalDateTime registeredAt;

    public UserSupplementResponse(Long userSupplementId, Long supplementId, String customName, String doseTime, LocalDateTime registeredAt) {
        this.userSupplementId = userSupplementId;
        this.supplementId = supplementId;
        this.customName = customName;
        this.doseTime = doseTime;
        this.registeredAt = registeredAt;
    }

    public Long getUserSupplementId() { return userSupplementId; }
    public Long getSupplementId() { return supplementId; }
    public String getCustomName() { return customName; }
    public String getDoseTime() { return doseTime; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
