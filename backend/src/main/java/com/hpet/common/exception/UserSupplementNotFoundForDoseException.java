package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class UserSupplementNotFoundForDoseException extends BusinessException {
    public UserSupplementNotFoundForDoseException(Long userSupplementId) {
        super(HttpStatus.NOT_FOUND, "USER_SUPPLEMENT_NOT_FOUND",
                "등록되지 않은 영양제입니다: userSupplementId=" + userSupplementId);
    }
}
