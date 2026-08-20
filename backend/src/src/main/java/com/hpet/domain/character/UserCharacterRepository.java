package com.hpet.domain.character;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {
    boolean existsByUserId(Long userId);
    Optional<UserCharacter> findByUserId(Long userId);
}
