package com.hpet.domain.posture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostureMonitoringSettingRepository extends JpaRepository<PostureMonitoringSetting, Long> {
    Optional<PostureMonitoringSetting> findByUserId(Long userId);
}
