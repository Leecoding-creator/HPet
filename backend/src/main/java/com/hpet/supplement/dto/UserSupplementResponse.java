package com.hpet.supplement.dto;

import java.time.LocalDateTime;

public class UserSupplementResponse {
    private final Long supplementId;
    private final String supplementName;
    private final LocalDateTime registeredAt;

    public UserSupplementResponse(Long supplementId, String supplementName, LocalDateTime registeredAt) {
        this.supplementId = supplementId;
        this.supplementName = supplementName;
        this.registeredAt = registeredAt;
    }

    public Long getSupplementId() { return supplementId; }
    public String getSupplementName() { return supplementName; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
