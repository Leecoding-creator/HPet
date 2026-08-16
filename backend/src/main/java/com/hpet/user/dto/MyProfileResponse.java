package com.hpet.user.dto;

import java.time.LocalDateTime;

public class MyProfileResponse {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String provider;
    private final boolean emailVerified;
    private final LocalDateTime createdAt;

    public MyProfileResponse(Long userId, String email, String nickname, String provider,
                              boolean emailVerified, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
    public String getProvider() { return provider; }
    public boolean isEmailVerified() { return emailVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
