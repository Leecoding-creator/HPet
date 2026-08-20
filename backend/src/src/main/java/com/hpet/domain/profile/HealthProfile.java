package com.hpet.domain.profile;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phase 2 - 2-1. 사용자 1명당 하나씩 존재하는 건강 프로필.
 * 저장 API는 upsert 방식(있으면 update, 없으면 새로 생성)으로 동작한다.
 */
@Entity
@Table(name = "health_profiles")
public class HealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Integer heightCm;

    private Integer weightKg;

    // 건강 고민/특이사항 자유 기재 (예: "허리 디스크 있음, 자세 안좋음")
    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected HealthProfile() {
        // JPA
    }

    public HealthProfile(Long userId) {
        this.userId = userId;
    }

    public void update(LocalDate birthDate, Gender gender, Integer heightCm, Integer weightKg, String memo) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.memo = memo;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
