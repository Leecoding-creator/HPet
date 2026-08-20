package com.hpet.posture;

import com.hpet.domain.posture.PostureEvent;
import com.hpet.domain.posture.PostureEventRepository;
import com.hpet.posture.dto.PostureEventCreateRequest;
import com.hpet.posture.dto.PostureEventResponse;
import com.hpet.posture.dto.PostureSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Phase 4 - 자세 불량 이벤트.
 * 판정 로직은 클라이언트에서 이미 끝낸 상태로 넘어오므로, 서버는 저장/조회/집계만 담당한다.
 */
@Service
public class PostureEventService {

    private static final Logger log = LoggerFactory.getLogger(PostureEventService.class);
    private static final int DEFAULT_RANGE_DAYS = 7;

    private final PostureEventRepository postureEventRepository;

    public PostureEventService(PostureEventRepository postureEventRepository) {
        this.postureEventRepository = postureEventRepository;
    }

    @Transactional
    public PostureEventResponse register(Long userId, PostureEventCreateRequest request) {
        PostureEvent event = new PostureEvent(userId, request.getDetectedAt(), request.getAngleDeg(), request.getDurationMin());
        PostureEvent saved = postureEventRepository.save(event);
        log.info("Posture event saved: userId={}, detectedAt={}, angleDeg={}", userId, saved.getDetectedAt(), saved.getAngleDeg());
        return toResponse(saved);
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
                event.getDurationMin(), event.getCreatedAt());
    }
}
