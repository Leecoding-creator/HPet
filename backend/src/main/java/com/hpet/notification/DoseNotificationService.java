package com.hpet.notification;

import com.hpet.common.exception.DoseNotificationNotFoundException;
import com.hpet.common.exception.DoseSupplementAccessDeniedException;
import com.hpet.common.exception.UserSupplementNotFoundForDoseException;
import com.hpet.domain.notification.DoseNotification;
import com.hpet.domain.notification.DoseNotificationRepository;
import com.hpet.domain.supplement.UserSupplement;
import com.hpet.domain.supplement.UserSupplementRepository;
import com.hpet.notification.dto.DoseNotificationRequest;
import com.hpet.notification.dto.DoseNotificationResponse;
import com.hpet.notification.dto.DoseNotificationUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Phase 5 - 5-1. 복용 알림 시간 CRUD.
 */
@Service
public class DoseNotificationService {

    private final DoseNotificationRepository doseNotificationRepository;
    private final UserSupplementRepository userSupplementRepository;

    public DoseNotificationService(DoseNotificationRepository doseNotificationRepository,
                                    UserSupplementRepository userSupplementRepository) {
        this.doseNotificationRepository = doseNotificationRepository;
        this.userSupplementRepository = userSupplementRepository;
    }

    @Transactional
    public DoseNotificationResponse create(Long userId, DoseNotificationRequest request) {
        UserSupplement userSupplement = userSupplementRepository.findById(request.getUserSupplementId())
                .orElseThrow(() -> new UserSupplementNotFoundForDoseException(request.getUserSupplementId()));

        if (!userSupplement.getUserId().equals(userId)) {
            throw new DoseSupplementAccessDeniedException();
        }

        DoseNotification notification = new DoseNotification(userId, userSupplement, request.getNotifyTime());
        DoseNotification saved = doseNotificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoseNotificationResponse> getMine(Long userId) {
        return doseNotificationRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoseNotificationResponse update(Long userId, Long notificationId, DoseNotificationUpdateRequest request) {
        DoseNotification notification = findOwned(userId, notificationId);
        notification.update(request.getNotifyTime(), request.isEnabled());
        return toResponse(notification);
    }

    @Transactional
    public void delete(Long userId, Long notificationId) {
        DoseNotification notification = findOwned(userId, notificationId);
        doseNotificationRepository.delete(notification);
    }

    private DoseNotification findOwned(Long userId, Long notificationId) {
        DoseNotification notification = doseNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new DoseNotificationNotFoundException(notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new DoseNotificationNotFoundException(notificationId); // 존재 자체를 숨김 (본인 것만 보이게)
        }
        return notification;
    }

    private DoseNotificationResponse toResponse(DoseNotification n) {
        return new DoseNotificationResponse(
                n.getId(), n.getUserSupplement().getId(), n.getUserSupplement().getSupplement().getName(),
                n.getNotifyTime(), n.isEnabled());
    }
}
