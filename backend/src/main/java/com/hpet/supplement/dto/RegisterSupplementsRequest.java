package com.hpet.supplement.dto;

import jakarta.validation.constraints.NotEmpty;

public class RegisterSupplementsRequest {

    @NotEmpty(message = "영양제 이름을 입력해주세요.")
    private String customName;

    @NotEmpty(message = "복용 시간을 입력해주세요.")
    private String doseTime;

    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }

    public String getDoseTime() { return doseTime; }
    public void setDoseTime(String doseTime) { this.doseTime = doseTime; }
}
