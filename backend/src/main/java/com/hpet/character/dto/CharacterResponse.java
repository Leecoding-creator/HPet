package com.hpet.character.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CharacterResponse {
    private final String characterCode;
    private final String characterName;
    private final String concept;
    private final int growthPoints;     // 누적 경험치 (0~300) - 2차 기획안 반영
    private final int maxGrowthPoints;  // 항상 300 - 프론트에서 진행률(%) 계산할 때 쓰라고 같이 내려줌
    private final String stage;         // STAGE_1, STAGE_2, STAGE_3, STAGE_4
    private final boolean canWearCostume;
    private final LocalDateTime assignedAt;
    private final List<String> equippedItems;

    public CharacterResponse(String characterCode, String characterName, String concept, int growthPoints,
                              int maxGrowthPoints, String stage, boolean canWearCostume,
                              LocalDateTime assignedAt, List<String> equippedItems) {
        this.characterCode = characterCode;
        this.characterName = characterName;
        this.concept = concept;
        this.growthPoints = growthPoints;
        this.maxGrowthPoints = maxGrowthPoints;
        this.stage = stage;
        this.canWearCostume = canWearCostume;
        this.assignedAt = assignedAt;
        this.equippedItems = equippedItems;
    }

    public String getCharacterCode() { return characterCode; }
    public String getCharacterName() { return characterName; }
    public String getConcept() { return concept; }
    public int getGrowthPoints() { return growthPoints; }
    public int getMaxGrowthPoints() { return maxGrowthPoints; }
    public String getStage() { return stage; }
    public boolean isCanWearCostume() { return canWearCostume; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public List<String> getEquippedItems() { return equippedItems; }
}
