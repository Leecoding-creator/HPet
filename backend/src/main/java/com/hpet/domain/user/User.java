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

    // 닉네임. 회원가입 시에는 비어있고, 프로필/설정에서 나중에 채워 넣는다.
    @Column(length = 30)
    private String nickname;

    // null이면 정상 회원, 값이 있으면 탈퇴한 회원 (소프트 삭제).
    // 탈퇴해도 복용기록/자세기록 등 히스토리 데이터는 그대로 남겨두기 위해 실제로 행을 지우지 않는다.
    private LocalDateTime deletedAt;

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

    public String getNickname() {
        return nickname;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 회원 탈퇴(소프트 삭제). 실제로 row를 지우지 않고 deletedAt만 채운다.
     * - 이렇게 해야 그동안의 복용기록/자세기록 등이 고아 데이터가 되지 않는다.
     * - refreshToken도 같이 지워서 이후 재로그인/토큰재발급이 안 되게 막는다.
     * - 이메일은 UserRepository의 *AndDeletedAtIsNull 계열 메서드로 조회하기 때문에,
     *   탈퇴 후에는 같은 이메일로 다시 회원가입할 수 있다.
     */
    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
        this.refreshToken = null;
    }
}
