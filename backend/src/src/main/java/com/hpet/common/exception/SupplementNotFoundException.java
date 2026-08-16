package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class SupplementNotFoundException extends BusinessException {
    public SupplementNotFoundException(Long supplementId) {
        super(HttpStatus.NOT_FOUND, "SUPPLEMENT_NOT_FOUND", "존재하지 않는 영양제입니다: id=" + supplementId);
    }
}
