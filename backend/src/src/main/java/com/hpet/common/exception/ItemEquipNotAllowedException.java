package com.hpet.common.exception;

import org.springframework.http.HttpStatus;

public class ItemEquipNotAllowedException extends BusinessException {
    public ItemEquipNotAllowedException() {
        super(HttpStatus.BAD_REQUEST, "ITEM_EQUIP_NOT_ALLOWED",
                "코스튬은 어린이 단계(7일차)부터 착용할 수 있습니다.");
    }
}
