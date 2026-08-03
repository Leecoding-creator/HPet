package com.hpet.character.dto;

import jakarta.validation.constraints.NotBlank;

public class EquipItemRequest {

    @NotBlank(message = "아이템 이름은 필수입니다.")
    private String itemName;

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
