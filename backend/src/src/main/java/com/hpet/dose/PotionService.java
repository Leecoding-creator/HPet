package com.hpet.dose;

import com.hpet.common.exception.CharacterNotAssignedException;
import com.hpet.domain.character.UserCharacter;
import com.hpet.domain.character.UserCharacterRepository;
import com.hpet.domain.dose.DoseMethod;
import com.hpet.domain.dose.DoseRecordRepository;
import com.hpet.domain.supplement.UserSupplementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Phase 5 - 5-8. 포션(성장치) 지급.
 *
 * 회의 1-3 확정 공식: 하루 성장치 총량 10점 / 등록 영양제 개수(n) = 인증 1회당 10/n점.
 * 부동소수점 오차를 피하려고 점수를 직접 더하는 대신 수학적으로 동치인 방식을 쓴다:
 * "오늘 사진 인증에 성공한 서로 다른 영양제 개수가 등록 개수(n) 이상이 되면 하루 완성"
 * → 등록 1개면 1번만 인증해도 바로 완성(=10점), 등록 3개면 3번 다 인증해야 완성(=10점),
 *   이는 매번 10/n점씩 쌓여 10점이 되는 것과 결과가 동일하다.
 */
@Service
public class PotionService {

    private static final Logger log = LoggerFactory.getLogger(PotionService.class);

    private final DoseRecordRepository doseRecordRepository;
    private final UserSupplementRepository userSupplementRepository;
    private final UserCharacterRepository userCharacterRepository;

    public PotionService(DoseRecordRepository doseRecordRepository,
                          UserSupplementRepository userSupplementRepository,
                          UserCharacterRepository userCharacterRepository) {
        this.doseRecordRepository = doseRecordRepository;
        this.userSupplementRepository = userSupplementRepository;
        this.userCharacterRepository = userCharacterRepository;
    }

    /**
     * 사진 인증 성공 직후 호출한다. 오늘치가 완성됐으면 캐릭터의 growthDays를 1 올린다
     * (이미 오늘 올렸으면 중복으로 올리지 않음 - UserCharacter.grantGrowthDayIfNotAlready 참고).
     */
    @Transactional
    public Result applyPotionForToday(Long userId) {
        LocalDate today = LocalDate.now();

        int requiredCount = userSupplementRepository.findByUserId(userId).size();
        int verifiedCount = (int) doseRecordRepository
                .countByUserIdAndDoseDateAndMethodAndVerifiedTrue(userId, today, DoseMethod.PHOTO);

        boolean dayCompleted = requiredCount > 0 && verifiedCount >= requiredCount;

        UserCharacter userCharacter = userCharacterRepository.findByUserId(userId)
                .orElseThrow(CharacterNotAssignedException::new);

        boolean newlyGranted = false;
        if (dayCompleted) {
            newlyGranted = userCharacter.grantGrowthDayIfNotAlready(today);
            if (newlyGranted) {
                log.info("Growth day granted: userId={}, growthDays={}", userId, userCharacter.getGrowthDays());
            }
        }

        return new Result(verifiedCount, requiredCount, dayCompleted, newlyGranted, userCharacter.getGrowthDays());
    }

    public record Result(int verifiedCountToday, int requiredCountToday, boolean dayCompleted,
                          boolean newlyGrantedToday, int growthDaysAfter) {
    }
}
