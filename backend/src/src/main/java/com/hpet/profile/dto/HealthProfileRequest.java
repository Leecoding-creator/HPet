package com.hpet.profile.dto;

import com.hpet.domain.profile.Gender;

import java.time.LocalDate;

public class HealthProfileRequest {

    private LocalDate birthDate;
    private Gender gender;
    private Integer heightCm;
    private Integer weightKg;
    private String memo;

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
