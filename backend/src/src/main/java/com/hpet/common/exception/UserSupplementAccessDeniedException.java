package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class UserSupplementAccessDeniedException extends BusinessException {
    public UserSupplementAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "USER_SUPPLEMENT_ACCESS_DENIED", "본인이 등록한 영양제가 아닙니다.");
    }
}
