package com.hpet.dose.dto;

import java.time.LocalDateTime;

public class DoseVerificationStatusResponse {
    private final Long userSupplementId;
    private final boolean verified;
    private final LocalDateTime verifiedAt;

    public DoseVerificationStatusResponse(Long userSupplementId, boolean verified, LocalDateTime verifiedAt) {
        this.userSupplementId = userSupplementId;
        this.verified = verified;
        this.verifiedAt = verifiedAt;
    }

    public Long getUserSupplementId() { return userSupplementId; }
    public boolean isVerified() { return verified; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
}
