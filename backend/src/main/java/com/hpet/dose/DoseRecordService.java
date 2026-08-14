package com.hpet.dose;

import com.hpet.common.exception.DuplicateDoseRecordException;
import com.hpet.common.exception.UserSupplementAccessDeniedException;
import com.hpet.common.exception.UserSupplementNotFoundException;
import com.hpet.domain.dose.DoseMethod;
import com.hpet.domain.dose.DoseRecord;
import com.hpet.domain.dose.DoseRecordRepository;
import com.hpet.domain.supplement.UserSupplement;
import com.hpet.domain.supplement.UserSupplementRepository;
import com.hpet.dose.dto.DoseRecordCreateRequest;
import com.hpet.dose.dto.DoseRecordResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 4 - 복용 기록 수동 체크.
 * 수동 등록은 별도 인증 절차 없이 등록 즉시 verified=true로 확정한다 (PHOTO 방식은 추후 별도 처리).
 */
@Service
public class DoseRecordService {

    private static final Logger log = LoggerFactory.getLogger(DoseRecordService.class);

    private final UserSupplementRepository userSupplementRepository;
    private final DoseRecordRepository doseRecordRepository;

    public DoseRecordService(UserSupplementRepository userSupplementRepository,
                              DoseRecordRepository doseRecordRepository) {
        this.userSupplementRepository = userSupplementRepository;
        this.doseRecordRepository = doseRecordRepository;
    }

    @Transactional
    public DoseRecordResponse registerManual(Long userId, DoseRecordCreateRequest request) {
        UserSupplement userSupplement = userSupplementRepository.findById(request.getUserSupplementId())
                .orElseThrow(() -> new UserSupplementNotFoundException(request.getUserSupplementId()));

        if (!userSupplement.getUserId().equals(userId)) {
            throw new UserSupplementAccessDeniedException();
        }

        LocalDate doseDate = request.getDoseDate() != null ? request.getDoseDate() : LocalDate.now();

        if (doseRecordRepository.existsByUserIdAndUserSupplementIdAndDoseDate(userId, userSupplement.getId(), doseDate)) {
            throw new DuplicateDoseRecordException(userSupplement.getId(), doseDate);
        }

        DoseRecord doseRecord = new DoseRecord(userId, userSupplement, doseDate, DoseMethod.MANUAL);
        doseRecord.markVerified();
        DoseRecord saved = doseRecordRepository.save(doseRecord);

        log.info("Dose record registered: userId={}, userSupplementId={}, doseDate={}", userId, userSupplement.getId(), doseDate);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoseRecordResponse> getByDate(Long userId, LocalDate doseDate) {
        LocalDate target = doseDate != null ? doseDate : LocalDate.now();
        return doseRecordRepository.findByUserIdAndDoseDate(userId, target).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 히스토리 캘린더 화면용 - 기간으로 조회.
     */
    @Transactional(readOnly = true)
    public List<DoseRecordResponse> getByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return doseRecordRepository.findByUserIdAndDoseDateBetween(userId, startDate, endDate).stream()
                .map(this::toResponse)
                .toList();
    }

    private DoseRecordResponse toResponse(DoseRecord doseRecord) {
        UserSupplement userSupplement = doseRecord.getUserSupplement();
        return new DoseRecordResponse(
                doseRecord.getId(),
                userSupplement.getId(),
                userSupplement.getSupplement().getName(),
                doseRecord.getDoseDate(),
                doseRecord.getMethod(),
                doseRecord.isVerified(),
                doseRecord.getVerifiedAt());
    }
}
