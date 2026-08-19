package com.hpet.domain.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    // 가장 최근에 발급된 미사용 코드를 찾는다 (재발송 시 이전 코드는 자연스럽게 무시됨)
    Optional<EmailVerificationCode> findTopByUserIdAndCodeAndUsedFalseOrderByCreatedAtDesc(Long userId, String code);
}
