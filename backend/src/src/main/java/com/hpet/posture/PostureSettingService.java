package com.hpet.posture;

import com.hpet.domain.posture.PostureMonitoringSetting;
import com.hpet.domain.posture.PostureMonitoringSettingRepository;
import com.hpet.posture.dto.PostureSettingRequest;
import com.hpet.posture.dto.PostureSettingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 4 - 자세 감지 활성 시간대 설정. 사용자당 설정 1개, upsert 방식 (HealthProfileService와 동일한 패턴).
 */
@Service
public class PostureSettingService {

    private final PostureMonitoringSettingRepository postureMonitoringSettingRepository;

    public PostureSettingService(PostureMonitoringSettingRepository postureMonitoringSettingRepository) {
        this.postureMonitoringSettingRepository = postureMonitoringSettingRepository;
    }

    @Transactional
    public PostureSettingResponse upsert(Long userId, PostureSettingRequest request) {
        PostureMonitoringSetting setting = postureMonitoringSettingRepository.findByUserId(userId)
                .orElseGet(() -> new PostureMonitoringSetting(userId));

        setting.update(request.getEnabled(), request.getStartTime(), request.getEndTime());

        PostureMonitoringSetting saved = postureMonitoringSettingRepository.save(setting);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PostureSettingResponse getMine(Long userId) {
        // 아직 설정을 저장한 적 없는 사용자는 감지가 꺼져 있는 기본 상태로 응답한다.
        return postureMonitoringSettingRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(() -> new PostureSettingResponse(false, null, null));
    }

    private PostureSettingResponse toResponse(PostureMonitoringSetting setting) {
        return new PostureSettingResponse(setting.isEnabled(), setting.getStartTime(), setting.getEndTime());
    }
}
