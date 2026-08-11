package com.hpet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Phase 5 - 5-2 NotificationScheduler(매분 알림 체크)를 동작시키기 위해 필요
public class HpetBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(HpetBackendApplication.class, args);
    }
}
