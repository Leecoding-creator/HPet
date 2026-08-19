package com.hpet.domain.character;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phase 2 - 2-5, 2-6. 사용자에게 배정된 캐릭터.
 * growthDays는 Phase 5의 포션 지급 API가 누적시키는 값이며,
 * 지금은 0으로 시작해서 아기 단계부터 보여준다.
 */
@Entity
@Table(name = "user_characters")
public class UserCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "characterId", nullable = false)
    private Character character;

    @Column(nullable = false)
    private int growthDays = 0;

    // 하루에 성장일수가 중복으로 올라가는 걸 막기 위한 마지막 지급일.
    // 오늘 이미 이 날짜로 채워졌으면 같은 날 또 growthDays를 올리지 않는다.
    private LocalDate lastGrowthDate;

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
     * 오늘 날짜분 성장치가 이미 채워졌으면 아무 것도 하지 않고 false를 반환한다(중복 지급 방지).
     * 아직이면 growthDays를 1 늘리고 lastGrowthDate를 오늘로 기록한 뒤 true를 반환한다.
     */
    public boolean grantGrowthDayIfNotAlready(LocalDate today) {
        if (today.equals(lastGrowthDate)) {
            return false;
        }
        this.growthDays++;
        this.lastGrowthDate = today;
        return true;
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

    public int getGrowthDays() {
        return growthDays;
    }

    public LocalDate getLastGrowthDate() {
        return lastGrowthDate;
    }

    public GrowthStage getStage() {
        return GrowthStage.fromDays(growthDays);
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
