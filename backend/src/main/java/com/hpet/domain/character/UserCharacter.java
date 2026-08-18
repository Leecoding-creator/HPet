package com.hpet.domain.character;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phase 2 - 2-5, 2-6. 사용자에게 배정된 캐릭터.
 *
 * ⚠️ 2차 기획안 반영 (준호님 요청, 2026-08-17): 1차 기획의 "growthDays(누적 완료일수, 정수)" 방식에서
 * "growthPoints(누적 경험치 0~300)" 방식으로 변경.
 * - 하루 최대 경험치 10점
 * - 등록 영양제 개수(n)에 따라 인증할 때마다 10/n점씩 획득 (예: 3개 등록 시 1개당 10/3 ≈ 3.3점)
 * - 인증 안 한 날은 그날 획득량 0
 * - 계산은 PotionService에서 담당 (반올림 오차 방지를 위해 "오늘 획득해야 할 총점"을 매번 다시 계산하는 방식 사용)
 */
@Entity
@Table(name = "user_characters")
public class UserCharacter {

    private static final int MAX_GROWTH_POINTS = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "characterId", nullable = false)
    private Character character;

    // 누적 경험치. 0~300 사이로 캡핑된다.
    @Column(nullable = false)
    private int growthPoints = 0;

    // 오늘 이미 지급받은 경험치 (하루 최대 10점 제한을 위해 필요).
    @Column(nullable = false)
    private int todayEarnedPoints = 0;

    // todayEarnedPoints가 어느 날짜 기준인지. 날짜가 바뀌면 todayEarnedPoints를 0으로 리셋해야 한다.
    private LocalDate lastEarnedDate;

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    protected UserCharacter() {
        // JPA
    }

    public UserCharacter(Long userId, Character character) {
        this.userId = userId;
        this.character = character;
    }

    /**
     * 오늘 획득해야 할 총 경험치(targetTodayPoints)를 매번 다시 계산해서 넘겨받고,
     * 지금까지 오늘 지급한 것(todayEarnedPoints)과의 차이만큼만 추가로 지급한다.
     * (중복 호출해도 안전 - 이미 반영된 만큼은 다시 안 더해짐)
     *
     * @return 실제로 추가된 경험치 (0이면 새로 늘어난 게 없다는 뜻)
     */
    public int syncTodayGrowthPoints(LocalDate today, int targetTodayPoints) {
        if (!today.equals(lastEarnedDate)) {
            this.todayEarnedPoints = 0;
            this.lastEarnedDate = today;
        }

        int delta = targetTodayPoints - todayEarnedPoints;
        if (delta <= 0) {
            return 0;
        }

        this.todayEarnedPoints = targetTodayPoints;
        this.growthPoints = Math.min(MAX_GROWTH_POINTS, this.growthPoints + delta);
        return delta;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Character getCharacter() {
        return character;
    }

    public int getGrowthPoints() {
        return growthPoints;
    }

    public int getTodayEarnedPoints() {
        return todayEarnedPoints;
    }

    public LocalDate getLastEarnedDate() {
        return lastEarnedDate;
    }

    public GrowthStage getStage() {
        return GrowthStage.fromPoints(growthPoints);
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
