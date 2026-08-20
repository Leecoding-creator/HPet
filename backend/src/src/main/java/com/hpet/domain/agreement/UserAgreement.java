package com.hpet.domain.agreement;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Phase 1 - 1-9. 약관/건강정보 동의 이력.
 * "현재 동의 상태"가 아니라 "이력(history)"이므로, 동의를 했든 철회(agreed=false)를 했든
 * 매번 새 레코드로 쌓는다. 어떤 버전의 약관에 언제 동의했는지 추적하기 위함.
 */
@Entity
@Table(name = "user_agreements")
public class UserAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgreementType type;

    @Column(nullable = false)
    private String version; // 예: "v1.0", "2026-08-01"

    @Column(nullable = false)
    private boolean agreed;

    @Column(nullable = false)
    private LocalDateTime agreedAt = LocalDateTime.now();

    protected UserAgreement() {
        // JPA
    }

    public UserAgreement(Long userId, AgreementType type, String version, boolean agreed) {
        this.userId = userId;
        this.type = type;
        this.version = version;
        this.agreed = agreed;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public AgreementType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public boolean isAgreed() {
        return agreed;
    }

    public LocalDateTime getAgreedAt() {
        return agreedAt;
    }
}
