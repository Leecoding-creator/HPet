package com.hpet.dose.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class DoseRecordCreateRequest {

    @NotNull(message = "userSupplementId는 필수입니다.")
    private Long userSupplementId;

    // 클라이언트가 보내지 않으면 서비스 레벨에서 오늘 날짜로 채워진다.
    private LocalDate doseDate;

    public Long getUserSupplementId() { return userSupplementId; }
    public void setUserSupplementId(Long userSupplementId) { this.userSupplementId = userSupplementId; }

    public LocalDate getDoseDate() { return doseDate; }
    public void setDoseDate(LocalDate doseDate) { this.doseDate = doseDate; }
}
