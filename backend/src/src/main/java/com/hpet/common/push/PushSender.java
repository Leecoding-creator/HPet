package com.hpet.common.push;

/**
 * FCM/APNs 발송을 추상화한 인터페이스. EmailSender와 동일한 패턴.
 * 지금은 ConsolePushSender(콘솔 로그)만 있지만, 실제 FCM 프로젝트 키가 준비되면
 * 이 인터페이스를 구현하는 FcmPushSender 같은 클래스만 새로 만들어 Bean을 교체하면 된다.
 */
public interface PushSender {
    void send(String deviceToken, String title, String body);
}
