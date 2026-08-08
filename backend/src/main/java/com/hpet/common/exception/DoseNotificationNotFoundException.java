package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class DoseNotificationNotFoundException extends BusinessException {
    public DoseNotificationNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "DOSE_NOTIFICATION_NOT_FOUND", "존재하지 않는 알림입니다: id=" + id);
    }
}
