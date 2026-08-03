package com.hpet.domain.supplement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplementRepository extends JpaRepository<Supplement, Long> {
    boolean existsByName(String name);
    Optional<Supplement> findByName(String name);
    List<Supplement> findByNameContaining(String keyword);
}
