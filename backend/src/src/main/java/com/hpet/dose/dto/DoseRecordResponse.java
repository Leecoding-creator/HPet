package com.hpet.dose.dto;

import com.hpet.domain.dose.DoseMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DoseRecordResponse {
    private final Long id;
    private final Long userSupplementId;
    private final String supplementName;
    private final LocalDate doseDate;
    private final DoseMethod method;
    private final boolean verified;
    private final LocalDateTime verifiedAt;

    public DoseRecordResponse(Long id, Long userSupplementId, String supplementName, LocalDate doseDate,
                               DoseMethod method, boolean verified, LocalDateTime verifiedAt) {
        this.id = id;
        this.userSupplementId = userSupplementId;
        this.supplementName = supplementName;
        this.doseDate = doseDate;
        this.method = method;
        this.verified = verified;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() { return id; }
    public Long getUserSupplementId() { return userSupplementId; }
    public String getSupplementName() { return supplementName; }
    public LocalDate getDoseDate() { return doseDate; }
    public DoseMethod getMethod() { return method; }
    public boolean isVerified() { return verified; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
}
