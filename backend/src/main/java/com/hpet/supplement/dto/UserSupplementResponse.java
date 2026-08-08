package com.hpet.supplement.dto;

import java.time.LocalDateTime;

public class UserSupplementResponse {
    private final Long userSupplementId; // 이 등록 항목 자체의 id - Phase 4/5 API(복용기록, 알림, 사진인증)에서 이 값을 씀
    private final Long supplementId;      // 영양제 마스터데이터 id
    private final String supplementName;
    private final LocalDateTime registeredAt;

    public UserSupplementResponse(Long userSupplementId, Long supplementId, String supplementName,
                                   LocalDateTime registeredAt) {
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
