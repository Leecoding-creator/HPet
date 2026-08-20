package com.hpet.character.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CharacterResponse {
    private final String characterCode;
    private final String characterName;
    private final String concept;
    private final int growthDays;
    private final String stage;         // BABY, TODDLER, CHILD, TEEN, ADULT, GROWN
    private final boolean canWearCostume;
    private final LocalDateTime assignedAt;
    private final List<String> equippedItems;

    public CharacterResponse(String characterCode, String characterName, String concept, int growthDays,
                              String stage, boolean canWearCostume, LocalDateTime assignedAt, List<String> equippedItems) {
        this.characterCode = characterCode;
        this.characterName = characterName;
        this.concept = concept;
        this.growthDays = growthDays;
        this.stage = stage;
        this.canWearCostume = canWearCostume;
        this.assignedAt = assignedAt;
        this.equippedItems = equippedItems;
    }

    public String getCharacterCode() { return characterCode; }
    public String getCharacterName() { return characterName; }
    public String getConcept() { return concept; }
    public int getGrowthDays() { return growthDays; }
    public String getStage() { return stage; }
    public boolean isCanWearCostume() { return canWearCostume; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public List<String> getEquippedItems() { return equippedItems; }
}
