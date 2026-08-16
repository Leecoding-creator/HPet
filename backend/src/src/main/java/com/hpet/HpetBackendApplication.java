package com.hpet;

import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Phase 5 - 5-2 NotificationScheduler(매분 알림 체크)를 동작시키기 위해 필요
public class HpetBackendApplication {
    public static void main(String[] args) {
        // spring-dotenv 5.1.0은 META-INF/spring.factories 자동 등록이 없어서
        // DotenvApplicationInitializer를 직접 등록해야 backend/.env가 실제로 로딩된다.
        new SpringApplicationBuilder(HpetBackendApplication.class)
                .initializers(new DotenvApplicationInitializer())
                .run(args);
    }
}
