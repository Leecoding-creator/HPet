package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class DoseSupplementAccessDeniedException extends BusinessException {
    public DoseSupplementAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "DOSE_SUPPLEMENT_ACCESS_DENIED",
                "본인이 등록한 영양제에 대해서만 복용 인증을 할 수 있습니다.");
    }
}
