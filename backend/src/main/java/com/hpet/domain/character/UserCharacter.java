package com.hpet.domain.character;

import jakarta.persistence.*;

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

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    protected UserCharacter() {
        // JPA
    }

    public UserCharacter(Long userId, Character character) {
        this.userId = userId;
        this.character = character;
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

    public GrowthStage getStage() {
        return GrowthStage.fromDays(growthDays);
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
