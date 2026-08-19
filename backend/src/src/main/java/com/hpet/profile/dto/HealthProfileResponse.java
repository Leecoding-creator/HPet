package com.hpet.profile.dto;

import com.hpet.domain.profile.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HealthProfileResponse {
    private final Long userId;
    private final LocalDate birthDate;
    private final Gender gender;
    private final Integer heightCm;
    private final Integer weightKg;
    private final String memo;
    private final LocalDateTime updatedAt;

    public HealthProfileResponse(Long userId, LocalDate birthDate, Gender gender, Integer heightCm,
                                  Integer weightKg, String memo, LocalDateTime updatedAt) {
        this.userId = userId;
        this.birthDate = birthDate;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.memo = memo;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() { return userId; }
    public LocalDate getBirthDate() { return birthDate; }
    public Gender getGender() { return gender; }
    public Integer getHeightCm() { return heightCm; }
    public Integer getWeightKg() { return weightKg; }
    public String getMemo() { return memo; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
