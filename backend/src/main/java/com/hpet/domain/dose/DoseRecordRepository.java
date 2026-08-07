package com.hpet.domain.dose;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoseRecordRepository extends JpaRepository<DoseRecord, Long> {
    List<DoseRecord> findByUserIdAndDoseDate(Long userId, LocalDate doseDate);
    boolean existsByUserIdAndUserSupplementIdAndDoseDate(Long userId, Long userSupplementId, LocalDate doseDate);
}
