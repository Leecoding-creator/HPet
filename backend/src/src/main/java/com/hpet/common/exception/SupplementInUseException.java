package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class SupplementInUseException extends BusinessException {
    public SupplementInUseException() {
        super(HttpStatus.CONFLICT, "SUPPLEMENT_IN_USE",
                "이 영양제와 연결된 복용 기록 또는 알림이 있어 삭제할 수 없습니다. 먼저 관련 알림을 삭제해주세요.");
    }
}
