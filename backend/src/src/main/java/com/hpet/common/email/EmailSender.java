package com.hpet.common.email;

/**
 * 메일 발송을 추상화한 인터페이스.
 * 지금은 ConsoleEmailSender(콘솔 로그) 구현체만 있지만,
 * 나중에 실제 SMTP/메일 서비스로 바꿀 때 이 인터페이스를 구현하는 클래스만
 * 새로 만들어서 Bean을 교체하면 서비스 로직(AuthService 등)은 손댈 필요가 없다.
 * (AiVisionClient와 동일한 패턴)
 */
public interface EmailSender {
    void send(String to, String subject, String body);
}
