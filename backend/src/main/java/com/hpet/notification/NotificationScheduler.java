package com.hpet.notification;

import com.hpet.common.push.PushSender;
import com.hpet.domain.notification.DeviceToken;
import com.hpet.domain.notification.DeviceTokenRepository;
import com.hpet.domain.notification.DoseNotification;
import com.hpet.domain.notification.DoseNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Phase 5 - 5-2. 매분 정각(0초)에 깨어나서, 설정된 알림 시간(시:분)과 지금 시각이 일치하는
 * 알림을 찾아 그 사용자의 등록된 기기 토큰으로 푸시를 발송한다.
 *
 * 지금은 PushSender가 ConsolePushSender(콘솔 로그)라서 실제 기기에 알림이 가진 않지만,
 * 이 스케줄러 자체는 실제 FCM 연동 후에도 그대로 재사용 가능하다.
 */
@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final DoseNotificationRepository doseNotificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    public NotificationScheduler(DoseNotificationRepository doseNotificationRepository,
                                  DeviceTokenRepository deviceTokenRepository,
                                  PushSender pushSender) {
        this.doseNotificationRepository = doseNotificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.pushSender = pushSender;
    }

    // 매분 0초에 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 * * * * *")
    public void dispatchDueNotifications() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        List<DoseNotification> enabledNotifications = doseNotificationRepository.findByEnabledTrue();
        for (DoseNotification notification : enabledNotifications) {
            LocalTime notifyTime = notification.getNotifyTime().withSecond(0).withNano(0);
            if (!notifyTime.equals(now)) {
                continue;
            }
            dispatch(notification);
        }
    }

    private void dispatch(DoseNotification notification) {
        String supplementName = notification.getUserSupplement().getCustomName();
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(notification.getUserId());

        if (tokens.isEmpty()) {
            log.info("등록된 기기 토큰이 없어 푸시를 건너뜁니다: userId={}", notification.getUserId());
            return;
        }

        for (DeviceToken token : tokens) {
            pushSender.send(
                    token.getToken(),
                    "[HPet] 복용할 시간이에요!",
                    supplementName + " 복용할 시간이에요. 사진으로 인증하고 포션을 받아보세요!"
            );
        }
    }
}
