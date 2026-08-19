package com.hpet.domain.verification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰. 랜덤 UUID 문자열이며, 1회용이다.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected PasswordResetToken() {
        // JPA
    }

    public PasswordResetToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void markUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
