package com.hpet.domain.posture;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Phase 4. 자세 불량 판정 이벤트.
 * 판정 로직은 클라이언트에서 이미 끝낸 상태로 전달되므로, 서버는 검증 없이 그대로 저장만 한다.
 */
@Entity
@Table(name = "posture_events")
public class PostureEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Column(nullable = false)
    private Integer angleDeg;

    @Column(nullable = false)
    private Integer durationMin;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 판정에 쓴 사진 저장 URL. 사진 기반 판정(POST /api/posture-events/photo)에서만 채워진다.
    private String imageUrl;

    protected PostureEvent() {
        // JPA
    }

    public PostureEvent(Long userId, LocalDateTime detectedAt, Integer angleDeg, Integer durationMin) {
        this.userId = userId;
        this.detectedAt = detectedAt;
        this.angleDeg = angleDeg;
        this.durationMin = durationMin;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public Integer getAngleDeg() {
        return angleDeg;
    }

    public Integer getDurationMin() {
        return durationMin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
