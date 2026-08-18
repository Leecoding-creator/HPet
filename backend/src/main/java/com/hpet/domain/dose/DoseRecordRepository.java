package com.hpet.domain.dose;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoseRecordRepository extends JpaRepository<DoseRecord, Long> {

    // --- 준호님 Phase 4 작업분 (원본 그대로) ---
    List<DoseRecord> findByUserIdAndDoseDate(Long userId, LocalDate doseDate);
    boolean existsByUserIdAndUserSupplementIdAndDoseDate(Long userId, Long userSupplementId, LocalDate doseDate);

    // --- 아래부터는 Phase 5(사진 인증·포션) 작업을 위해 경진이 추가한 메서드입니다.
    //     준호님 브랜치의 실제 파일과 병합할 때 이 부분만 옮겨 붙이면 됩니다. ---

    // 사진 인증 흐름에서 "오늘 이 영양제에 대한 기록이 이미 있는지"를 찾아서
    // 없으면 새로 만들고, 있으면(예: 이전 시도 후 verified=false로 남아있는 경우) 재사용한다.
    Optional<DoseRecord> findByUserIdAndUserSupplementIdAndDoseDate(Long userId, Long userSupplementId, LocalDate doseDate);

    // 포션 계산: 오늘 사진 인증에 성공한(method=PHOTO, verified=true) 서로 다른 영양제 개수를 센다.
    long countByUserIdAndDoseDateAndMethodAndVerifiedTrue(Long userId, LocalDate doseDate, DoseMethod method);

    // Phase 5-9. 영양제 등록 취소(삭제) 전에, 관련 복용 기록이 있는지 확인하는 용도.
    boolean existsByUserSupplementId(Long userSupplementId);

    // 히스토리 캘린더 조회용 - 기간(startDate~endDate)으로 조회.
    // (원래 findByUserIdAndDoseDate 하루치만 있었는데, 캘린더 화면에서 한 달치를 한 번에 봐야 해서 추가함)
    List<DoseRecord> findByUserIdAndDoseDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
