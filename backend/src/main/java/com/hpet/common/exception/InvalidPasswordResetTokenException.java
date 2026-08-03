package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends BusinessException {
    public InvalidPasswordResetTokenException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN", "재설정 토큰이 올바르지 않거나 만료되었습니다.");
    }
}
