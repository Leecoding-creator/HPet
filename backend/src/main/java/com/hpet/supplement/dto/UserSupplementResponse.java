package com.hpet.supplement.dto;

import java.time.LocalDateTime;

public class UserSupplementResponse {
    private final Long userSupplementId;
    private final Long supplementId;
    private final String supplementName;
    private final LocalDateTime registeredAt;

    public UserSupplementResponse(Long userSupplementId, Long supplementId, String supplementName, LocalDateTime registeredAt) {
        this.userSupplementId = userSupplementId;
        this.supplementId = supplementId;
        this.supplementName = supplementName;
        this.registeredAt = registeredAt;
    }

    public Long getUserSupplementId() { return userSupplementId; }
    public Long getSupplementId() { return supplementId; }
    public String getSupplementName() { return supplementName; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
