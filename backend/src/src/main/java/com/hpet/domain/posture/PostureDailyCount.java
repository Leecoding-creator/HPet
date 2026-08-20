package com.hpet.domain.posture;

import java.time.LocalDate;

/**
 * PostureEventRepository#countByDetectedDate의 인터페이스 기반 프로젝션.
 * JPQL의 별칭(date, count)과 getter 이름이 매칭되어야 한다.
 */
public interface PostureDailyCount {
    LocalDate getDate();
    Long getCount();
}
