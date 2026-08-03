package com.hpet.domain.character;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Phase 2 - 2-7. 캐릭터 코스튬(장착 아이템). 어린이 단계(7일차)부터 착용 가능.
 */
@Entity
@Table(name = "user_character_items")
public class UserCharacterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userCharacterId;

    @Column(nullable = false)
    private String itemName; // 예: "머리핀", "리본"

    @Column(nullable = false)
    private boolean equipped = true;

    @Column(nullable = false)
    private LocalDateTime equippedAt = LocalDateTime.now();

    protected UserCharacterItem() {
        // JPA
    }

    public UserCharacterItem(Long userCharacterId, String itemName) {
        this.userCharacterId = userCharacterId;
        this.itemName = itemName;
    }

    public Long getId() {
        return id;
    }

    public Long getUserCharacterId() {
        return userCharacterId;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void unequip() {
        this.equipped = false;
    }

    public LocalDateTime getEquippedAt() {
        return equippedAt;
    }
}
