package com.hpet.domain.posture;

import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * Phase 4. 사용자 1명당 하나씩 존재하는 자세 감지 활성 시간대 설정.
 * 저장 API는 upsert 방식으로 동작한다 (HealthProfile과 동일한 패턴).
 */
@Entity
@Table(name = "posture_monitoring_settings")
public class PostureMonitoringSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private boolean enabled;

    private LocalTime startTime;

    private LocalTime endTime;

    protected PostureMonitoringSetting() {
        // JPA
    }

    public PostureMonitoringSetting(Long userId) {
        this.userId = userId;
    }

    public void update(boolean enabled, LocalTime startTime, LocalTime endTime) {
        this.enabled = enabled;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
