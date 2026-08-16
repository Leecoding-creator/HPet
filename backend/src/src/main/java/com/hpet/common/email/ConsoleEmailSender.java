package com.hpet.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 해커톤 데모용 임시 구현체.
 * 실제로 메일을 보내지 않고, 서버 콘솔에 로그로만 찍는다.
 * 개발자가 이 로그를 보고 인증코드/재설정 토큰을 확인해서 Swagger로 직접 테스트하면 된다.
 *
 * 실제 메일 서버(SMTP)가 준비되면 이 클래스 대신 SmtpEmailSender 같은 걸 새로 만들어서
 * @Primary 를 붙이거나 이 클래스를 지우기만 하면 나머지 코드는 그대로 재사용된다.
 */
@Component
public class ConsoleEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("=== [MOCK EMAIL] ===================================");
        log.info("To      : {}", to);
        log.info("Subject : {}", subject);
        log.info("Body    : {}", body);
        log.info("=====================================================");
    }
}
