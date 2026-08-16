package com.hpet.common.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 해커톤 데모용 임시 구현체. 실제 FCM/APNs 서버에 보내지 않고 콘솔 로그로만 출력한다.
 * FCM 프로젝트(서버 키, google-services.json 등)가 준비되면 이 클래스 대신
 * 실제 Firebase Admin SDK를 쓰는 구현체로 교체하면 되고, 나머지 코드는 그대로 재사용된다.
 */
@Component
public class ConsolePushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(ConsolePushSender.class);

    @Override
    public void send(String deviceToken, String title, String body) {
        log.info("=== [MOCK PUSH] ===================================");
        log.info("To (token) : {}", deviceToken);
        log.info("Title      : {}", title);
        log.info("Body       : {}", body);
        log.info("=====================================================");
    }
}
