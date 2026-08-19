package com.hpet.config;

import com.hpet.domain.character.Character;
import com.hpet.domain.character.CharacterCode;
import com.hpet.domain.character.CharacterRepository;
import com.hpet.domain.supplement.Supplement;
import com.hpet.domain.supplement.SupplementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 앱 기동 시 영양제/캐릭터 마스터 데이터를 시드로 넣어준다.
 * 이미 데이터가 있으면(재기동 시) 건너뛴다.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    // 회의 1-2 확정: 유산균은 MVP 캐릭터 매핑에서 제외하지만, 마스터 데이터 자체는 등록해둔다.
    private static final List<String> SUPPLEMENT_NAMES = List.of(
            "철분", "칼슘", "오메가3", "비타민", "마그네슘", "멜라토닌", "비오틴", "유산균"
    );

    private final SupplementRepository supplementRepository;
    private final CharacterRepository characterRepository;

    public DataSeeder(SupplementRepository supplementRepository, CharacterRepository characterRepository) {
        this.supplementRepository = supplementRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    public void run(String... args) {
        seedSupplements();
        seedCharacters();
    }

    private void seedSupplements() {
        for (String name : SUPPLEMENT_NAMES) {
            if (!supplementRepository.existsByName(name)) {
                supplementRepository.save(new Supplement(name));
                log.info("Seeded supplement: {}", name);
            }
        }
    }

    private void seedCharacters() {
        seedCharacterIfAbsent(CharacterCode.TURTLE, "거북이", "딱딱한 이미지");
        seedCharacterIfAbsent(CharacterCode.CHICK, "병아리", "노란색");
        seedCharacterIfAbsent(CharacterCode.OTTER, "수달", "마음이 편안해짐");
        seedCharacterIfAbsent(CharacterCode.HEDGEHOG, "고슴도치", "머리카락이 윤기나게 자람");
    }

    private void seedCharacterIfAbsent(String code, String name, String concept) {
        if (!characterRepository.existsByCode(code)) {
            characterRepository.save(new Character(code, name, concept));
            log.info("Seeded character: {} ({})", name, code);
        }
    }
}
