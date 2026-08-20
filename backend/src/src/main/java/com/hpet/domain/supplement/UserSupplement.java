package com.hpet.domain.supplement;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 사용자가 등록한 영양제. 캐릭터 배정(character 패키지)과
 * 나중에 Phase 5 포션 계산의 기준(등록 개수 n)이 된다.
 */
@Entity
@Table(name = "user_supplements", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "supplementId"}))
public class UserSupplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplementId", nullable = false)
    private Supplement supplement;

    @Column(nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    protected UserSupplement() {
        // JPA
    }

    public UserSupplement(Long userId, Supplement supplement) {
        this.userId = userId;
        this.supplement = supplement;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Supplement getSupplement() {
        return supplement;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
