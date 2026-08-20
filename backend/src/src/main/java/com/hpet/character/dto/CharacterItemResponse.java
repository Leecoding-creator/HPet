package com.hpet.character.dto;

public class CharacterItemResponse {
    private final Long itemId;
    private final String itemName;

    public CharacterItemResponse(Long itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    public Long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
}
