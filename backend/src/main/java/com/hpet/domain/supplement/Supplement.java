package com.hpet.domain.supplement;

import jakarta.persistence.*;

/**
 * Phase 2 - 2-2. 영양제 마스터 데이터 (전체 사용자 공통).
 * 앱 시작 시 DataSeeder가 기본 8종을 등록해둔다.
 */
@Entity
@Table(name = "supplements")
public class Supplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Supplement() {
        // JPA
    }

    public Supplement(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
