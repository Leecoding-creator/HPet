package com.hpet.supplement.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class RegisterSupplementsRequest {

    @NotEmpty(message = "등록할 영양제를 최소 1개 이상 선택해주세요.")
    private List<Long> supplementIds;

    public List<Long> getSupplementIds() { return supplementIds; }
    public void setSupplementIds(List<Long> supplementIds) { this.supplementIds = supplementIds; }
}
