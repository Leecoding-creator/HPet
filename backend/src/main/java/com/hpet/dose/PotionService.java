package com.hpet.dose;

import com.hpet.common.exception.CharacterNotAssignedException;
import com.hpet.domain.character.UserCharacter;
import com.hpet.domain.character.UserCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Phase 5 - 5-8. 포션(성장치) 지급.
 *
 * ⚠️ 2차 기획안 반영 (준호님 요청, 2026-08-17): "완료일수(growthDays) 정수 카운트" → "누적 경험치(0~300)".
 * ⚠️ 팀 확정(2026-08-19, 준호님): 영양제 인증이 "영양제별 개별 사진"에서 "전체(통째로) 인증,
 * AI가 알약 개수만 세는 방식"으로 바뀌면서, 이 서비스도 DoseRecord를 세는 대신 그날 몇 개까지
 * 인증됐는지를 파라미터로 직접 받는 방식으로 바뀜 (실제 카운트/기록은 DoseVerificationService가 담당).
 *
 * 확정 공식:
 *   - 하루 최대 획득 경험치: 10점
 *   - 등록 영양제 개수(n)에 따라, 오늘 인증된 개수(0~n)에 비례해서 10 * (인증개수/n)점 획득
 *   - 인증 안 한 날은 그날 획득량 0
 *
 * 계산 방식: 매번 "오늘 획득해야 할 총점"을 처음부터 다시 계산해서(반올림), 지금까지 지급된 것과의
 * 차이만큼만 추가로 지급한다 (UserCharacter.syncTodayGrowthPoints 참고).
 */
@Service
public class PotionService {

    private static final Logger log = LoggerFactory.getLogger(PotionService.class);
    private static final int DAILY_MAX_POINTS = 10;

    private final UserCharacterRepository userCharacterRepository;

    public PotionService(UserCharacterRepository userCharacterRepository) {
        this.userCharacterRepository = userCharacterRepository;
    }

    /**
     * 사진 인증 성공 직후 호출한다.
     *
     * @param verifiedCount 오늘 최종적으로 인증된 개수 (여러 번 나눠 찍은 경우 누적된 값, requiredCount로 캡됨)
     * @param requiredCount 오늘 등록된 영양제 개수(n)
     */
    @Transactional
    public Result applyPotionForToday(Long userId, int verifiedCount, int requiredCount) {
        LocalDate today = LocalDate.now();

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

        return new Result(verifiedCount, requiredCount, dayCompleted, addedPoints > 0,
                userCharacter.getGrowthPoints(), addedPoints, userCharacter.getTodayEarnedPoints(), DAILY_MAX_POINTS);
    }

    public record Result(int verifiedCountToday, int requiredCountToday, boolean dayCompleted,
                          boolean newlyGrantedToday, int growthPointsAfter,
                          int pointsGainedThisTime, int todayEarnedPoints, int dailyMaxPoints) {
    }
}
