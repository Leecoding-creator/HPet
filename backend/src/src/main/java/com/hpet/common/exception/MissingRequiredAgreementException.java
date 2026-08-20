package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class MissingRequiredAgreementException extends BusinessException {
    public MissingRequiredAgreementException(String missingType) {
        super(HttpStatus.BAD_REQUEST, "MISSING_REQUIRED_AGREEMENT",
                "필수 동의 항목이 누락되었거나 동의하지 않았습니다: " + missingType);
    }
}
