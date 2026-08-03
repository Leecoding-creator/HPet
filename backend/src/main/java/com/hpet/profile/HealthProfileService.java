package com.hpet.profile;

import com.hpet.domain.profile.HealthProfile;
import com.hpet.domain.profile.HealthProfileRepository;
import com.hpet.profile.dto.HealthProfileRequest;
import com.hpet.profile.dto.HealthProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 2 - 2-1. 건강 프로필 저장/조회. 있으면 update, 없으면 새로 생성(upsert).
 */
@Service
public class HealthProfileService {

    private final HealthProfileRepository healthProfileRepository;

    public HealthProfileService(HealthProfileRepository healthProfileRepository) {
        this.healthProfileRepository = healthProfileRepository;
    }

    @Transactional
    public HealthProfileResponse upsert(Long userId, HealthProfileRequest request) {
        HealthProfile profile = healthProfileRepository.findByUserId(userId)
                .orElseGet(() -> new HealthProfile(userId));

        profile.update(request.getBirthDate(), request.getGender(), request.getHeightCm(),
                request.getWeightKg(), request.getMemo());

        HealthProfile saved = healthProfileRepository.save(profile);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public HealthProfileResponse getMyProfile(Long userId) {
        return healthProfileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    private HealthProfileResponse toResponse(HealthProfile profile) {
        return new HealthProfileResponse(
                profile.getUserId(), profile.getBirthDate(), profile.getGender(),
                profile.getHeightCm(), profile.getWeightKg(), profile.getMemo(), profile.getUpdatedAt());
    }
}
