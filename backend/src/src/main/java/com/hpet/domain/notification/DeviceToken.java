package com.hpet.domain.notification;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Phase 5 - 5-2. FCM/APNs로 푸시를 보낼 대상 기기 토큰.
 * 앱(프론트)이 로그인 후 발급받은 FCM 토큰을 여기로 등록해두면 스케줄러가 이걸 보고 발송한다.
 */
@Entity
@Table(name = "device_tokens")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 300)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DevicePlatform platform;

    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    protected DeviceToken() {
        // JPA
    }

    public DeviceToken(Long userId, String token, DevicePlatform platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
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

    public DevicePlatform getPlatform() {
        return platform;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
