package com.hpet.home;

import com.hpet.character.CharacterService;
import com.hpet.character.dto.CharacterResponse;
import com.hpet.domain.dose.DoseRecord;
import com.hpet.domain.dose.DoseRecordRepository;
import com.hpet.domain.posture.PostureEventRepository;
import com.hpet.domain.supplement.UserSupplement;
import com.hpet.domain.supplement.UserSupplementRepository;
import com.hpet.home.dto.HomeSummaryResponse;
import com.hpet.posture.dto.PostureSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Phase 3. 홈 대시보드 요약 - 오늘 복용 현황(DoseRecordRepository/UserSupplementRepository),
 * 캐릭터 상태(CharacterService 재사용), 최근 자세 교정 현황(PostureEventRepository의 집계 쿼리 재사용)을
 * 한 화면 응답으로 조합한다. 각 도메인의 조회 로직은 새로 구현하지 않고 기존 Repository/Service를 그대로 사용한다.
 */
@Service
public class HomeSummaryService {

    private static final int POSTURE_SUMMARY_RANGE_DAYS = 7;

    private final DoseRecordRepository doseRecordRepository;
    private final UserSupplementRepository userSupplementRepository;
    private final PostureEventRepository postureEventRepository;
    private final CharacterService characterService;

    public HomeSummaryService(DoseRecordRepository doseRecordRepository,
                               UserSupplementRepository userSupplementRepository,
                               PostureEventRepository postureEventRepository,
                               CharacterService characterService) {
        this.doseRecordRepository = doseRecordRepository;
        this.userSupplementRepository = userSupplementRepository;
        this.postureEventRepository = postureEventRepository;
        this.characterService = characterService;
    }

    @Transactional(readOnly = true)
    public HomeSummaryResponse getSummary(Long userId) {
        return new HomeSummaryResponse(buildDoseSummary(userId), buildCharacterSummary(userId), buildPostureSummary(userId));
    }

    private HomeSummaryResponse.DoseSummary buildDoseSummary(Long userId) {
        List<UserSupplement> registeredSupplements = userSupplementRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();

        Set<Long> completedUserSupplementIds = doseRecordRepository.findByUserIdAndDoseDate(userId, today).stream()
                .filter(DoseRecord::isVerified)
                .map(record -> record.getUserSupplement().getId())
                .collect(Collectors.toSet());

        List<HomeSummaryResponse.DoseItem> doseList = registeredSupplements.stream()
                .map(us -> new HomeSummaryResponse.DoseItem(
                        us.getId(), us.getCustomName(), completedUserSupplementIds.contains(us.getId())))
                .toList();

        return new HomeSummaryResponse.DoseSummary(registeredSupplements.size(), completedUserSupplementIds.size(), doseList);
    }

    private CharacterResponse buildCharacterSummary(Long userId) {
        // 아직 매핑되는 영양제를 등록하지 않아 캐릭터가 배정되지 않은 사용자도 홈 화면은 정상적으로 떠야 하므로
        // 예외를 던지는 getMyCharacter() 대신 findMyCharacter()로 조회해서 없으면 null을 그대로 내려준다.
        return characterService.findMyCharacter(userId).orElse(null);
    }

    private HomeSummaryResponse.PostureSummary buildPostureSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate rangeStart = today.minusDays(POSTURE_SUMMARY_RANGE_DAYS - 1);
        LocalDateTime start = rangeStart.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<PostureSummaryResponse> dailyCounts = postureEventRepository.countByDetectedDate(userId, start, end).stream()
                .map(row -> new PostureSummaryResponse(row.getDate(), row.getCount()))
                .toList();

        int todayCount = dailyCounts.stream()
                .filter(daily -> daily.getDate().equals(today))
                .mapToInt(daily -> (int) daily.getCount())
                .findFirst()
                .orElse(0);

        return new HomeSummaryResponse.PostureSummary(todayCount, dailyCounts);
    }
}
