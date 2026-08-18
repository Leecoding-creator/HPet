package com.hpet.domain.supplement;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 사용자가 등록한 영양제. 캐릭터 배정(character 패키지)과
 * 나중에 Phase 5 포션 계산의 기준(등록 개수 n)이 된다.
 */
@Entity
@Table(name = "user_supplements") // unique constraint 제거
public class UserSupplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplementId", nullable = true)
    private Supplement supplement;

    @Column(nullable = false)
    private String customName;

    @Column(nullable = false)
    private String doseTime;

    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    protected UserSupplement() {
        // JPA
    }

    public UserSupplement(Long userId, Supplement supplement, String customName, String doseTime) {
        this.userId = userId;
        this.supplement = supplement;
        this.customName = customName;
        this.doseTime = doseTime;
    }

    public void update(Supplement supplement, String customName, String doseTime) {
        this.supplement = supplement;
        this.customName = customName;
        this.doseTime = doseTime;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Supplement getSupplement() {
        return supplement;
    }

    public String getCustomName() {
        return customName;
    }

    public String getDoseTime() {
        return doseTime;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
