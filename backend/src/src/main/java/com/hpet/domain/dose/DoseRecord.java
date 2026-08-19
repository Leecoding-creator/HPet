package com.hpet.domain.dose;

import com.hpet.domain.supplement.UserSupplement;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Phase 4. 특정 날짜에 특정(사용자가 등록한) 영양제를 복용했다는 인증 기록.
 * method가 MANUAL이면 등록 즉시 verified=true로 처리하고, PHOTO는 이후 별도 인증 절차를 거친다.
 */
@Entity
@Table(name = "dose_records", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "user_supplement_id", "doseDate"}))
public class DoseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_supplement_id", nullable = false)
    private UserSupplement userSupplement;

    @Column(nullable = false)
    private LocalDate doseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoseMethod method;

    @Column(nullable = false)
    private boolean verified = false;

    private LocalDateTime verifiedAt;

    protected DoseRecord() {
        // JPA
    }

    public DoseRecord(Long userId, UserSupplement userSupplement, LocalDate doseDate, DoseMethod method) {
        this.userId = userId;
        this.userSupplement = userSupplement;
        this.doseDate = doseDate;
        this.method = method;
        this.verified = false;
    }

    public void markVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public UserSupplement getUserSupplement() {
        return userSupplement;
    }

    public LocalDate getDoseDate() {
        return doseDate;
    }

    public DoseMethod getMethod() {
        return method;
    }

    public boolean isVerified() {
        return verified;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}
