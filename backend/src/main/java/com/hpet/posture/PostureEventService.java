package com.hpet.posture;

import com.hpet.common.storage.ImageStorageService;
import com.hpet.domain.posture.PostureEvent;
import com.hpet.domain.posture.PostureEventRepository;
import com.hpet.posture.dto.PostureEventCreateRequest;
import com.hpet.posture.dto.PostureEventResponse;
import com.hpet.posture.dto.PostureSummaryResponse;
import com.hpet.posture.dto.PostureVerificationResponse;
import com.hpet.vision.AiVisionClient;
import com.hpet.vision.VisionJudgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Phase 4 - 자세 불량 이벤트.
 *
 * ⚠️ 방향 전환(팀 결정): 원래 계획서는 "폰 기울기 센서 + 화면 on 30분 유지"를 클라이언트(네이티브 앱)가
 * 상시 백그라운드로 판정하는 구조였다. 지금은 웹 프로토타입이라 그 방식이 불가능해서,
 * "사용자가 직접 사진을 찍어 올리면 AI가 그 사진 한 장으로 거북목 여부를 판정"하는 방식으로 바꿨다.
 * (판정 대상이 "지속적인 자세"에서 "한 순간의 사진"으로 바뀐 것 - 나중에 네이티브 앱으로 갈 때 원래 방식 재검토 필요)
 *
 * register()(기존 자가 판정 결과 저장)와 verifyPhoto()(신규 사진 기반 AI 판정) 둘 다 남겨뒀다.
 */
@Service
public class PostureEventService {

    private static final Logger log = LoggerFactory.getLogger(PostureEventService.class);
    private static final int DEFAULT_RANGE_DAYS = 7;

    private final PostureEventRepository postureEventRepository;
    private final AiVisionClient aiVisionClient;
    private final ImageStorageService imageStorageService;

    public PostureEventService(PostureEventRepository postureEventRepository,
                                AiVisionClient aiVisionClient,
                                ImageStorageService imageStorageService) {
        this.postureEventRepository = postureEventRepository;
        this.aiVisionClient = aiVisionClient;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public PostureEventResponse register(Long userId, PostureEventCreateRequest request) {
        PostureEvent event = new PostureEvent(userId, request.getDetectedAt(), request.getAngleDeg(), request.getDurationMin());
        PostureEvent saved = postureEventRepository.save(event);
        log.info("Posture event saved: userId={}, detectedAt={}, angleDeg={}", userId, saved.getDetectedAt(), saved.getAngleDeg());
        return toResponse(saved);
    }

    /**
     * 사진 한 장을 업로드하면 AI가 거북목 여부를 판정한다.
     * 거북목이 감지되면(=success) PostureEvent를 만들어 저장하고 사진도 저장한다.
     * 감지 안 되면(바른 자세) 아무것도 저장하지 않는다.
     */
    @Transactional
    public PostureVerificationResponse verifyPhoto(Long userId, MultipartFile image) {
        byte[] imageBytes = readBytes(image);
        String mimeType = image.getContentType() != null ? image.getContentType() : "image/jpeg";

        VisionJudgement judgement = aiVisionClient.judgePosture(imageBytes, mimeType);

        if (!judgement.success()) {
            log.info("Posture check: 바른 자세로 판정됨. userId={}", userId);
            return new PostureVerificationResponse(false, judgement.reason(), null);
        }

        String imageUrl = imageStorageService.save("posture", imageBytes);

        // 사진 한 장으로 판정하는 구조라 실제 각도/지속시간을 측정할 수 없다.
        // angleDeg=0(측정불가), durationMin=0(스냅샷이라 지속시간 개념 없음)으로 기록하고,
        // "감지됐다는 사실 자체"에 의미를 둔다 (집계 API는 발생 횟수만 세므로 문제 없음).
        PostureEvent event = new PostureEvent(userId, LocalDateTime.now(), 0, 0);
        event.setImageUrl(imageUrl);
        PostureEvent saved = postureEventRepository.save(event);
        log.info("거북목 감지됨, 저장함: userId={}, imageUrl={}", userId, imageUrl);

        return new PostureVerificationResponse(true, judgement.reason(), imageUrl);
    }

    @Transactional(readOnly = true)
    public List<PostureEventResponse> getHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = resolveRange(startDate, endDate);
        return postureEventRepository
                .findByUserIdAndDetectedAtBetween(userId, range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostureSummaryResponse> getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = resolveRange(startDate, endDate);
        return postureEventRepository
                .countByDetectedDate(userId, range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX))
                .stream()
                .map(row -> new PostureSummaryResponse(row.getDate(), row.getCount()))
                .toList();
    }

    /**
     * 기간 파라미터가 없으면 오늘을 포함한 최근 7일(오늘-6일 ~ 오늘)을 기본값으로 사용한다.
     */
    private LocalDate[] resolveRange(LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStart = startDate != null ? startDate : resolvedEnd.minusDays(DEFAULT_RANGE_DAYS - 1);
        return new LocalDate[] { resolvedStart, resolvedEnd };
    }

    private PostureEventResponse toResponse(PostureEvent event) {
        return new PostureEventResponse(event.getId(), event.getDetectedAt(), event.getAngleDeg(),
                event.getDurationMin(), event.getCreatedAt(), event.getImageUrl());
    }

    private byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일을 읽는 데 실패했습니다.", e);
        }
    }
}
