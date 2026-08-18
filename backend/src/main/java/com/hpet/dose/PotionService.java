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
 * ⚠️ 2차 기획안 반영 (준호님 요청, 2026-08-17): 1차 기획의 "완료일수(growthDays) 정수 카운트" 방식에서
 * "누적 경험치(growthPoints, 0~300)" 방식으로 변경.
 *
 * 확정 공식:
 *   - 하루 최대 획득 경험치: 10점
 *   - 등록 영양제 개수(n)에 따라 인증 1회당 10/n점 획득 (예: 3개 등록 시 1개당 10/3 ≈ 3.3점)
 *   - 인증 안 한 날은 그날 획득량 0
 *
 * 계산 방식: 매번 "오늘 획득해야 할 총점"을 처음부터 다시 계산해서(반올림), 지금까지 지급된 것과의
 * 차이만큼만 추가로 지급한다 (UserCharacter.syncTodayGrowthPoints 참고). 이렇게 하면 10/n을 여러 번
 * 나눠서 더할 때 생기는 반올림 오차가 누적되지 않는다.
 *   예) 3개 등록: 1개 인증 시 오늘 총점 = round(10 * 1/3) = 3점 지급
 *       2개 인증 시 오늘 총점 = round(10 * 2/3) = 7점 → 기존 3점에서 4점 추가 지급
 *       3개 인증 시 오늘 총점 = round(10 * 3/3) = 10점 → 기존 7점에서 3점 추가 지급 (합계 정확히 10점)
 */
@Service
public class PotionService {

    private static final Logger log = LoggerFactory.getLogger(PotionService.class);
    private static final int DAILY_MAX_POINTS = 10;

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
     * 사진 인증 성공 직후 호출한다. 오늘 획득해야 할 경험치를 재계산해서 부족한 만큼 지급한다.
     */
    @Transactional
    public Result applyPotionForToday(Long userId) {
        LocalDate today = LocalDate.now();

        int requiredCount = userSupplementRepository.findByUserId(userId).size();
        int verifiedCount = (int) doseRecordRepository
                .countByUserIdAndDoseDateAndMethodAndVerifiedTrue(userId, today, DoseMethod.PHOTO);

        int targetTodayPoints = 0;
        if (requiredCount > 0) {
            targetTodayPoints = Math.min(DAILY_MAX_POINTS,
                    (int) Math.round(DAILY_MAX_POINTS * (double) verifiedCount / requiredCount));
        }
        boolean dayCompleted = targetTodayPoints >= DAILY_MAX_POINTS;

        UserCharacter userCharacter = userCharacterRepository.findByUserId(userId)
                .orElseThrow(CharacterNotAssignedException::new);

        int addedPoints = userCharacter.syncTodayGrowthPoints(today, targetTodayPoints);
        if (addedPoints > 0) {
            log.info("Growth points granted: userId={}, +{}점, 누적={}점",
                    userId, addedPoints, userCharacter.getGrowthPoints());
        }

        // 프론트 캐릭터 모션(포션 섭취 애니메이션)에서 "오늘 획득 성장치 +N/10"을 그대로 표시할 수 있게
        // 이번 호출로 실제 늘어난 점수(pointsGainedThisTime)와 오늘 누적치(todayEarnedPoints)를 같이 내려준다.
        return new Result(verifiedCount, requiredCount, dayCompleted, addedPoints > 0,
                userCharacter.getGrowthPoints(), addedPoints, userCharacter.getTodayEarnedPoints(), DAILY_MAX_POINTS);
    }

    public record Result(int verifiedCountToday, int requiredCountToday, boolean dayCompleted,
                          boolean newlyGrantedToday, int growthPointsAfter,
                          int pointsGainedThisTime, int todayEarnedPoints, int dailyMaxPoints) {
    }
}
