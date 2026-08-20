package com.hpet.domain.posture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostureEventRepository extends JpaRepository<PostureEvent, Long> {

    List<PostureEvent> findByUserIdAndDetectedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT CAST(p.detectedAt AS date) AS date, COUNT(p) AS count " +
            "FROM PostureEvent p " +
            "WHERE p.userId = :userId AND p.detectedAt BETWEEN :start AND :end " +
            "GROUP BY CAST(p.detectedAt AS date) " +
            "ORDER BY CAST(p.detectedAt AS date)")
    List<PostureDailyCount> countByDetectedDate(@Param("userId") Long userId,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}
