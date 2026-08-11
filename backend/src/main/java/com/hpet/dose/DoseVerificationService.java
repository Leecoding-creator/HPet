package com.hpet.dose;

import com.hpet.common.exception.DoseSupplementAccessDeniedException;
import com.hpet.common.exception.UserSupplementNotFoundForDoseException;
import com.hpet.domain.dose.DoseMethod;
import com.hpet.domain.dose.DoseRecord;
import com.hpet.domain.dose.DoseRecordRepository;
import com.hpet.domain.supplement.UserSupplement;
import com.hpet.domain.supplement.UserSupplementRepository;
import com.hpet.dose.dto.DoseVerificationResponse;
import com.hpet.dose.dto.DoseVerificationStatusResponse;
import com.hpet.vision.AiVisionClient;
import com.hpet.vision.VisionJudgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Phase 5 - 5-3~5-7. 이미지 업로드 → AI 판정 → DoseRecord 반영 → 포션 지급까지 한 번에 처리한다.
 *
 * (해커톤 스코프 단순화) 원래 계획은 S3 Presigned URL 업로드 + 폴링 방식 결과 조회였지만,
 * 시간 관계상 "업로드와 동시에 동기 호출로 즉시 결과 반환"으로 단순화했다.
 * 사진 원본은 저장하지 않고 AI 호출에만 사용한다 (개인정보/용량 이슈 - 회의 1-7 로깅 원칙과 동일한 이유).
 */
@Service
public class DoseVerificationService {

    private static final Logger log = LoggerFactory.getLogger(DoseVerificationService.class);

    private final UserSupplementRepository userSupplementRepository;
    private final DoseRecordRepository doseRecordRepository;
    private final AiVisionClient aiVisionClient;
    private final PotionService potionService;

    public DoseVerificationService(UserSupplementRepository userSupplementRepository,
                                    DoseRecordRepository doseRecordRepository,
                                    AiVisionClient aiVisionClient,
                                    PotionService potionService) {
        this.userSupplementRepository = userSupplementRepository;
        this.doseRecordRepository = doseRecordRepository;
        this.aiVisionClient = aiVisionClient;
        this.potionService = potionService;
    }

    @Transactional
    public DoseVerificationResponse verifyPhoto(Long userId, Long userSupplementId, MultipartFile image) {
        UserSupplement userSupplement = userSupplementRepository.findById(userSupplementId)
                .orElseThrow(() -> new UserSupplementNotFoundForDoseException(userSupplementId));

        if (!userSupplement.getUserId().equals(userId)) {
            throw new DoseSupplementAccessDeniedException();
        }

        LocalDate today = LocalDate.now();

        // 이미 오늘 이 영양제로 인증 성공한 기록이 있으면 재인증 없이 그대로 응답한다 (중복 방지, 멱등 처리).
        Optional<DoseRecord> existing = doseRecordRepository
                .findByUserIdAndUserSupplementIdAndDoseDate(userId, userSupplementId, today);
        if (existing.isPresent() && existing.get().isVerified()) {
            log.info("Already verified today: userId={}, userSupplementId={}", userId, userSupplementId);
            PotionService.Result result = potionService.applyPotionForToday(userId);
            return toResponse(true, "오늘 이미 인증이 완료된 영양제입니다.", result);
        }

        byte[] imageBytes = readBytes(image);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        VisionJudgement judgement = aiVisionClient.judge(imageBytes, mimeType, userSupplement.getSupplement().getName());

        if (!judgement.success()) {
            log.info("Verification failed: userId={}, userSupplementId={}, reason={}",
                    userId, userSupplementId, judgement.reason());
            return new DoseVerificationResponse(false, judgement.reason(), 0, 0, false, 0);
        }

        DoseRecord record = existing.orElseGet(() -> new DoseRecord(userId, userSupplement, today, DoseMethod.PHOTO));
        record.markVerified();
        if (existing.isEmpty()) {
            doseRecordRepository.save(record);
        }
        log.info("Verification succeeded: userId={}, userSupplementId={}", userId, userSupplementId);

        PotionService.Result result = potionService.applyPotionForToday(userId);
        return toResponse(true, judgement.reason(), result);
    }

    @Transactional(readOnly = true)
    public DoseVerificationStatusResponse getStatus(Long userId, Long userSupplementId) {
        LocalDate today = LocalDate.now();
        return doseRecordRepository.findByUserIdAndUserSupplementIdAndDoseDate(userId, userSupplementId, today)
                .map(r -> new DoseVerificationStatusResponse(userSupplementId, r.isVerified(), r.getVerifiedAt()))
                .orElse(new DoseVerificationStatusResponse(userSupplementId, false, null));
    }

    private DoseVerificationResponse toResponse(boolean verified, String reason, PotionService.Result result) {
        return new DoseVerificationResponse(
                verified, reason, result.verifiedCountToday(), result.requiredCountToday(),
                result.dayCompleted(), result.growthDaysAfter());
    }

    private byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일을 읽는 데 실패했습니다.", e);
        }
    }
}
