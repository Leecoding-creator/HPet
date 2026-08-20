package com.hpet.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoseNotificationRepository extends JpaRepository<DoseNotification, Long> {
    List<DoseNotification> findByUserId(Long userId);
    List<DoseNotification> findByEnabledTrue();
    boolean existsByUserSupplementId(Long userSupplementId);
}
