package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class UserSupplementNotFoundException extends BusinessException {
    public UserSupplementNotFoundException(Long userSupplementId) {
        super(HttpStatus.NOT_FOUND, "USER_SUPPLEMENT_NOT_FOUND", "등록된 영양제가 아닙니다: id=" + userSupplementId);
    }
}
