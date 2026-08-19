package com.hpet.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ReissueRequest {

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
