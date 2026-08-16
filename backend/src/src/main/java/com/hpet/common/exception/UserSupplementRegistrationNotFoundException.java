package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class UserSupplementRegistrationNotFoundException extends BusinessException {
    public UserSupplementRegistrationNotFoundException(Long userSupplementId) {
        super(HttpStatus.NOT_FOUND, "USER_SUPPLEMENT_NOT_FOUND",
                "등록되지 않은 영양제이거나 본인이 등록한 항목이 아닙니다: id=" + userSupplementId);
    }
}
