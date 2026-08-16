package com.hpet.domain.character;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCharacterItemRepository extends JpaRepository<UserCharacterItem, Long> {
    List<UserCharacterItem> findByUserCharacterIdAndEquippedTrue(Long userCharacterId);
}
