package com.hpet.domain.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.EMAIL;

    // 로그인 시 발급된 refreshToken을 저장해두고, /reissue 시 일치하는지 검사한다.
    // 로그아웃하면 null로 초기화해서 재사용을 막는다.
    @Column(length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected User() {
        // JPA
    }

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.provider = AuthProvider.EMAIL;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
