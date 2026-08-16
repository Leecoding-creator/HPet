package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends BusinessException {
    public InvalidVerificationCodeException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE", "인증코드가 올바르지 않거나 만료되었습니다.");
    }
}
