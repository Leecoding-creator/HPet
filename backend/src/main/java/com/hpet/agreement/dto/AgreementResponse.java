package com.hpet.agreement.dto;

import com.hpet.domain.agreement.AgreementType;

import java.time.LocalDateTime;

public class AgreementResponse {
    private final AgreementType type;
    private final String version;
    private final boolean agreed;
    private final LocalDateTime agreedAt;

    public AgreementResponse(AgreementType type, String version, boolean agreed, LocalDateTime agreedAt) {
        this.type = type;
        this.version = version;
        this.agreed = agreed;
        this.agreedAt = agreedAt;
    }

    public AgreementType getType() { return type; }
    public String getVersion() { return version; }
    public boolean isAgreed() { return agreed; }
    public LocalDateTime getAgreedAt() { return agreedAt; }
}
