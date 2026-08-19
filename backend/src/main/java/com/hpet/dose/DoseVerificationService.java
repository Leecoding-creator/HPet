package com.hpet.dose;

import com.hpet.common.storage.ImageStorageService;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Phase 5 - 5-3~5-7. 이미지 업로드 → AI 판정(알약 개수 세기) → 포션 지급까지 한 번에 처리한다.
 *
 * ⚠️ 팀 확정(2026-08-19, 준호님과의 대화):
 *   1. "영양제별로 따로 안 하고 그냥 통째로(전체) 인증" — AI는 색깔로 대강 짐작만 가능하지
 *      정확히 무슨 영양제인지 구별할 수 없으므로, 애초에 구별하려 하지 않고 "알약이 몇 개
 *      보이는지"만 센다. 그래서 사진 인증은 특정 영양제 하나를 지정하는 게 아니라 "오늘"
 *      단위로 이뤄진다.
 *   2. "같은 사진 재사용 안 했는지만 체크" — 인증 자체의 진위 여부는 AI가 아니라 "똑같은
 *      사진 파일을 다른 날짜에 재사용했는지"로 막는다. (사진 원본의 SHA-256 해시 비교)
 *   3. "영양제가 2개 이상이면 시간대가 다를 수 있어서 여러 번 나눠 찍을 수 있다" — 그래서
 *      이번 판정 개수는 "오늘 총합"이 아니라 "이번에 새로 인증하는 몫"으로 보고 누적한다.
 *      나눠서 찍든 한 번에 찍든 최종 점수는 등록 개수(n)로 캡되므로 동일하다.
 *
 * 인증 결과는 준호님이 설계하신 공용 DoseRecord에 그대로 기록한다 (별도 테이블을 만들지 않음 -
 * 그래야 이력 캘린더/홈 요약 등 다른 화면에서도 이 인증 결과가 일관되게 보인다).
 * 어떤 영양제가 구체적으로 인증됐는지는 AI가 구별 못 하므로, "아직 오늘 인증 안 된 영양제"
 * 중에서 인증된 개수만큼 순서대로 채운다.
 */
@Service
public class DoseVerificationService {

    private static final Logger log = LoggerFactory.getLogger(DoseVerificationService.class);

    private final UserSupplementRepository userSupplementRepository;
    private final DoseRecordRepository doseRecordRepository;
    private final AiVisionClient aiVisionClient;
    private final PotionService potionService;
    private final ImageStorageService imageStorageService;

    public DoseVerificationService(UserSupplementRepository userSupplementRepository,
                                    DoseRecordRepository doseRecordRepository,
                                    AiVisionClient aiVisionClient,
                                    PotionService potionService,
                                    ImageStorageService imageStorageService) {
        this.userSupplementRepository = userSupplementRepository;
        this.doseRecordRepository = doseRecordRepository;
        this.aiVisionClient = aiVisionClient;
        this.potionService = potionService;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public DoseVerificationResponse verifyPhoto(Long userId, MultipartFile image) {
        LocalDate today = LocalDate.now();

        List<UserSupplement> registered = userSupplementRepository.findByUserId(userId);
        int requiredCount = registered.size();

        byte[] imageBytes = readBytes(image);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        // 사진 부정 재사용 방지: AI 호출 전에 먼저 "오늘이 아닌 다른 날짜"에 이미 쓰인 사진인지 확인한다.
        // (재사용이면 AI 호출 자체를 안 해서 비용도 아낀다)
        String imageHash = computeSha256(imageBytes);
        boolean isReusedPhoto = doseRecordRepository.existsByUserIdAndImageHashAndDoseDateNot(userId, imageHash, today);
        if (isReusedPhoto) {
            log.info("Duplicate photo reuse detected: userId={}", userId);
            return new DoseVerificationResponse(false,
                    "이미 다른 날짜에 인증에 사용된 사진이에요. 오늘 다시 촬영해서 올려주세요.",
                    0, requiredCount, false, 0, null, 0, 0, 10);
        }

        VisionJudgement judgement = aiVisionClient.judgePillCount(imageBytes, mimeType);

        if (!judgement.success()) {
            log.info("Verification failed: userId={}, reason={}", userId, judgement.reason());
            return new DoseVerificationResponse(false, judgement.reason(), 0, requiredCount, false, 0, null, 0, 0, 10);
        }

        // 오늘 이미 PHOTO로 인증된 영양제들 파악 (다른 화면/이력에서도 그대로 보이는 공용 DoseRecord 기준)
        List<DoseRecord> todayRecords = doseRecordRepository.findByUserIdAndDoseDate(userId, today);
        Set<Long> alreadyVerifiedSupplementIds = todayRecords.stream()
                .filter(r -> r.getMethod() == DoseMethod.PHOTO && r.isVerified())
                .map(r -> r.getUserSupplement().getId())
                .collect(Collectors.toSet());

        int existingVerifiedCount = alreadyVerifiedSupplementIds.size();
        // 영양제가 2개 이상이면 시간대가 달라서 한 번에 다 못 찍을 수 있다(예: 비타민은 아침, 마그네슘은 밤).
        // 그래서 이번 판정 개수는 "오늘 총합"이 아니라 "이번에 새로 인증하는 몫"으로 보고 누적한다.
        int targetVerifiedCount = Math.min(requiredCount, existingVerifiedCount + judgement.pillCount());

        String imageUrl = imageStorageService.save("dose", imageBytes);

        int newlyNeeded = targetVerifiedCount - existingVerifiedCount;
        if (newlyNeeded > 0) {
            List<UserSupplement> notYetVerified = registered.stream()
                    .filter(s -> !alreadyVerifiedSupplementIds.contains(s.getId()))
                    .limit(newlyNeeded)
                    .toList();

            for (UserSupplement supplement : notYetVerified) {
                Optional<DoseRecord> existing = todayRecords.stream()
                        .filter(r -> r.getUserSupplement().getId().equals(supplement.getId()))
                        .findFirst();

                DoseRecord record = existing.orElseGet(() -> new DoseRecord(userId, supplement, today, DoseMethod.PHOTO));
                record.markVerified();
                record.setImageUrl(imageUrl);
                record.setImageHash(imageHash);
                if (existing.isEmpty()) {
                    doseRecordRepository.save(record);
                }
            }
        }
        log.info("Verification succeeded: userId={}, aiCount={}, targetVerifiedCount={}/{}, imageUrl={}",
                userId, judgement.pillCount(), targetVerifiedCount, requiredCount, imageUrl);

        PotionService.Result result = potionService.applyPotionForToday(userId, targetVerifiedCount, requiredCount);
        return toResponse(true, judgement.reason(), result, imageUrl);
    }

    @Transactional(readOnly = true)
    public DoseVerificationStatusResponse getStatus(Long userId) {
        LocalDate today = LocalDate.now();
        int requiredCount = userSupplementRepository.findByUserId(userId).size();

        List<DoseRecord> verifiedToday = doseRecordRepository.findByUserIdAndDoseDate(userId, today).stream()
                .filter(r -> r.getMethod() == DoseMethod.PHOTO && r.isVerified())
                .toList();

        int pillCount = verifiedToday.size();
        String imageUrl = verifiedToday.stream()
                .map(DoseRecord::getImageUrl)
                .filter(url -> url != null)
                .reduce((first, second) -> second) // 가장 마지막(최근) 걸로
                .orElse(null);
        LocalDateTime verifiedAt = verifiedToday.stream()
                .map(DoseRecord::getVerifiedAt)
                .filter(at -> at != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new DoseVerificationStatusResponse(pillCount, requiredCount, imageUrl, verifiedAt);
    }

    private DoseVerificationResponse toResponse(boolean verified, String reason, PotionService.Result result, String imageUrl) {
        return new DoseVerificationResponse(
                verified, reason, result.verifiedCountToday(), result.requiredCountToday(),
                result.dayCompleted(), result.growthPointsAfter(), imageUrl,
                result.pointsGainedThisTime(), result.todayEarnedPoints(), result.dailyMaxPoints());
    }

    private byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일을 읽는 데 실패했습니다.", e);
        }
    }

    private String computeSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("해시 계산에 실패했습니다.", e);
        }
    }
}
