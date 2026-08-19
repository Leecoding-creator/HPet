package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class HealthProfileNotFoundException extends BusinessException {
    public HealthProfileNotFoundException() {
        super(HttpStatus.NOT_FOUND, "HEALTH_PROFILE_NOT_FOUND",
                "건강 프로필을 먼저 등록해주세요. (POST /api/profile)");
    }
}
