package com.hpet.domain.supplement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSupplementRepository extends JpaRepository<UserSupplement, Long> {
    List<UserSupplement> findByUserId(Long userId);
    boolean existsByUserIdAndSupplement(Long userId, Supplement supplement);
}
