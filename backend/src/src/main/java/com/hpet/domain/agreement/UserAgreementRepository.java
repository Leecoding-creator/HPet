package com.hpet.domain.agreement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {
    List<UserAgreement> findByUserIdOrderByAgreedAtDesc(Long userId);
}
