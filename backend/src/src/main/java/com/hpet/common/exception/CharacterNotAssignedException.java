package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class CharacterNotAssignedException extends BusinessException {
    public CharacterNotAssignedException() {
        super(HttpStatus.NOT_FOUND, "CHARACTER_NOT_ASSIGNED",
                "아직 배정된 캐릭터가 없습니다. 매핑되는 영양제를 먼저 등록해주세요.");
    }
}
