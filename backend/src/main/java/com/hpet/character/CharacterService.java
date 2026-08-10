package com.hpet.character;

import com.hpet.character.dto.CharacterItemResponse;
import com.hpet.character.dto.CharacterResponse;
import com.hpet.character.dto.EquipItemRequest;
import com.hpet.common.exception.CharacterNotAssignedException;
import com.hpet.common.exception.ItemEquipNotAllowedException;
import com.hpet.domain.character.GrowthStage;
import com.hpet.domain.character.UserCharacter;
import com.hpet.domain.character.UserCharacterItem;
import com.hpet.domain.character.UserCharacterItemRepository;
import com.hpet.domain.character.UserCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Phase 2 - 2-6, 2-7. 캐릭터 조회 + 코스튬 착용/해제.
 */
@Service
public class CharacterService {

    private static final Logger log = LoggerFactory.getLogger(CharacterService.class);

    private final UserCharacterRepository userCharacterRepository;
    private final UserCharacterItemRepository userCharacterItemRepository;

    public CharacterService(UserCharacterRepository userCharacterRepository,
                             UserCharacterItemRepository userCharacterItemRepository) {
        this.userCharacterRepository = userCharacterRepository;
        this.userCharacterItemRepository = userCharacterItemRepository;
    }

    @Transactional(readOnly = true)
    public CharacterResponse getMyCharacter(Long userId) {
        return findMyCharacter(userId).orElseThrow(CharacterNotAssignedException::new);
    }

    /**
     * 홈 대시보드처럼 캐릭터 미배정 상태도 정상 흐름으로 다뤄야 하는 호출부를 위한 조회.
     * getMyCharacter()와 달리 미배정이어도 예외를 던지지 않고 Optional.empty()를 반환한다.
     */
    @Transactional(readOnly = true)
    public Optional<CharacterResponse> findMyCharacter(Long userId) {
        return userCharacterRepository.findByUserId(userId).map(this::toResponse);
    }

    private CharacterResponse toResponse(UserCharacter userCharacter) {
        GrowthStage stage = userCharacter.getStage();
        List<String> equippedItemNames = userCharacterItemRepository
                .findByUserCharacterIdAndEquippedTrue(userCharacter.getId()).stream()
                .map(UserCharacterItem::getItemName)
                .toList();

        return new CharacterResponse(
                userCharacter.getCharacter().getCode(),
                userCharacter.getCharacter().getName(),
                userCharacter.getCharacter().getConcept(),
                userCharacter.getGrowthDays(),
                stage.name(),
                stage.canWearCostume(),
                userCharacter.getAssignedAt(),
                equippedItemNames
        );
    }

    @Transactional
    public CharacterItemResponse equipItem(Long userId, EquipItemRequest request) {
        UserCharacter userCharacter = userCharacterRepository.findByUserId(userId)
                .orElseThrow(CharacterNotAssignedException::new);

        if (!userCharacter.getStage().canWearCostume()) {
            throw new ItemEquipNotAllowedException();
        }

        UserCharacterItem item = userCharacterItemRepository.save(
                new UserCharacterItem(userCharacter.getId(), request.getItemName()));
        log.info("Item equipped: userId={}, item={}", userId, request.getItemName());
        return new CharacterItemResponse(item.getId(), item.getItemName());
    }

    @Transactional
    public void unequipItem(Long userId, Long itemId) {
        UserCharacter userCharacter = userCharacterRepository.findByUserId(userId)
                .orElseThrow(CharacterNotAssignedException::new);

        userCharacterItemRepository.findById(itemId)
                .filter(item -> item.getUserCharacterId().equals(userCharacter.getId()))
                .ifPresent(UserCharacterItem::unequip);
    }
}
