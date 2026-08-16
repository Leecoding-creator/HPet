package com.hpet.domain.notification;

import com.hpet.domain.supplement.UserSupplement;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Phase 5 - 5-1. 복용 알림 시간. 사용자가 등록한 영양제 하나마다 원하는 시간에 알림을 설정할 수 있다.
 */
@Entity
@Table(name = "dose_notifications")
public class DoseNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_supplement_id", nullable = false)
    private UserSupplement userSupplement;

    @Column(nullable = false)
    private LocalTime notifyTime;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected DoseNotification() {
        // JPA
    }

    public DoseNotification(Long userId, UserSupplement userSupplement, LocalTime notifyTime) {
        this.userId = userId;
        this.userSupplement = userSupplement;
        this.notifyTime = notifyTime;
    }

    public void update(LocalTime notifyTime, boolean enabled) {
        this.notifyTime = notifyTime;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public UserSupplement getUserSupplement() {
        return userSupplement;
    }

    public LocalTime getNotifyTime() {
        return notifyTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
