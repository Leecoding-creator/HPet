package com.hpet.domain.verification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 이메일 인증코드. 회원가입 직후(또는 재발송 요청 시) 발급되며,
 * 유효시간이 지나거나 이미 사용됐으면 재사용할 수 없다.
 */
@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected EmailVerificationCode() {
        // JPA
    }

    public EmailVerificationCode(Long userId, String code, LocalDateTime expiresAt) {
        this.userId = userId;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
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
