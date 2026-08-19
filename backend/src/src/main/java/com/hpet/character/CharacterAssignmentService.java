package com.hpet.character;

import com.hpet.domain.character.Character;
import com.hpet.domain.character.CharacterCode;
import com.hpet.domain.character.CharacterRepository;
import com.hpet.domain.character.UserCharacter;
import com.hpet.domain.character.UserCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 회의 1-2 확정 로직.
 * - 매핑표: 거북이(철분/칼슘/오메가3), 병아리(비타민), 수달(마그네슘/멜라토닌), 고슴도치(비오틴)
 * - 유산균은 매핑 대상에서 제외.
 * - 등록한 영양제가 여러 캐릭터에 매칭되면, 매칭 후보 중 랜덤으로 배정 (흥미 유발 목적).
 */
@Service
public class CharacterAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(CharacterAssignmentService.class);

    private static final Map<String, Set<String>> CHARACTER_SUPPLEMENT_MAP = Map.of(
            CharacterCode.TURTLE, Set.of("철분", "칼슘", "오메가3"),
            CharacterCode.CHICK, Set.of("비타민"),
            CharacterCode.OTTER, Set.of("마그네슘", "멜라토닌"),
            CharacterCode.HEDGEHOG, Set.of("비오틴")
    );

    private final CharacterRepository characterRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final SecureRandom random = new SecureRandom();

    public CharacterAssignmentService(CharacterRepository characterRepository, UserCharacterRepository userCharacterRepository) {
        this.characterRepository = characterRepository;
        this.userCharacterRepository = userCharacterRepository;
    }

    /**
     * 등록된 영양제 이름 목록을 기준으로 후보 캐릭터를 찾고, 여러 개면 랜덤으로 하나를 골라
     * UserCharacter를 새로 만든다. 이미 배정되어 있으면 아무 것도 하지 않는다(멱등).
     *
     * @return 새로 배정됐으면 UserCharacter, 매칭되는 후보가 없거나 이미 배정돼 있으면 Optional.empty()
     */
    @Transactional
    public Optional<UserCharacter> assignIfNeeded(Long userId, List<String> registeredSupplementNames) {
        if (userCharacterRepository.existsByUserId(userId)) {
            return Optional.empty();
        }

        List<String> candidateCodes = findCandidateCharacterCodes(registeredSupplementNames);
        if (candidateCodes.isEmpty()) {
            return Optional.empty();
        }

        String chosenCode = candidateCodes.get(random.nextInt(candidateCodes.size()));
        Character character = characterRepository.findByCode(chosenCode)
                .orElseThrow(() -> new IllegalStateException("시드 데이터 누락: " + chosenCode));

        UserCharacter userCharacter = userCharacterRepository.save(new UserCharacter(userId, character));
        log.info("Character assigned: userId={}, character={} (candidates={})", userId, chosenCode, candidateCodes);
        return Optional.of(userCharacter);
    }

    private List<String> findCandidateCharacterCodes(List<String> registeredSupplementNames) {
        Set<String> registeredSet = Set.copyOf(registeredSupplementNames);
        List<String> candidates = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : CHARACTER_SUPPLEMENT_MAP.entrySet()) {
            boolean matches = entry.getValue().stream().anyMatch(registeredSet::contains);
            if (matches) {
                candidates.add(entry.getKey());
            }
        }
        return candidates;
    }
}
