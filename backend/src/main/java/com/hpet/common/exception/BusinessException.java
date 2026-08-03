package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인/비즈니스 로직에서 의도적으로 던지는 예외의 베이스 클래스.
 * 각 구체 예외는 HTTP status + 에러코드 + 메시지를 갖는다.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
