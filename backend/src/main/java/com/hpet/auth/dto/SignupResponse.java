package com.hpet.auth.dto;

public class SignupResponse {
    private final Long userId;
    private final String email;

    public SignupResponse(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
