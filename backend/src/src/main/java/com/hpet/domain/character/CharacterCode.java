package com.hpet.domain.character;

/**
 * 회의 1-2 확정: 기본 캐릭터 4종. 이후 유니콘/드래곤 등 확장 예정이라 enum이 아니라
 * DB 테이블(Character 엔티티)로 관리하지만, 코드값은 이 상수를 그대로 사용한다.
 */
public final class CharacterCode {
    public static final String TURTLE = "TURTLE";     // 거북이
    public static final String CHICK = "CHICK";       // 병아리
    public static final String OTTER = "OTTER";       // 수달
    public static final String HEDGEHOG = "HEDGEHOG"; // 고슴도치

    private CharacterCode() {
    }
}
