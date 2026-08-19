package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public class DuplicateDoseRecordException extends BusinessException {
    public DuplicateDoseRecordException(Long userSupplementId, LocalDate doseDate) {
        super(HttpStatus.CONFLICT, "DUPLICATE_DOSE_RECORD",
                "이미 등록된 복용 기록입니다: userSupplementId=" + userSupplementId + ", doseDate=" + doseDate);
    }
}
