package com.hpet.domain.character;

import jakarta.persistence.*;

/**
 * Phase 2 - 2-5. 캐릭터 마스터 데이터 (거북이/병아리/수달/고슴도치 4종).
 * DataSeeder가 앱 시작 시 시드 데이터를 넣어준다.
 */
@Entity
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // CharacterCode 상수 값

    @Column(nullable = false)
    private String name; // 거북이, 병아리, 수달, 고슴도치

    @Column(nullable = false)
    private String concept; // 컨셉 설명 (예: "딱딱한 이미지")

    protected Character() {
        // JPA
    }

    public Character(String code, String name, String concept) {
        this.code = code;
        this.name = name;
        this.concept = concept;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getConcept() {
        return concept;
    }
}
