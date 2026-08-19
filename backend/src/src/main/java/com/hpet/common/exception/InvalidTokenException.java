package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", message);
    }
}
