package com.hpet.domain.character;

/**
 * ⚠️ 2차 기획안 반영 (준호님 요청, 2026-08-17): 1차 기획의 6단계(BABY~GROWN, 날짜 기준)에서
 * 4단계(경험치 0~300 기준)로 재설계.
 *
 * 구간:
 *   1단계: 0~49
 *   2단계: 50~119
 *   3단계: 120~199
 *   4단계: 200~300
 *
 * ⚠️ 단계 이름은 2차 기획안에 명시된 게 없어서 일단 "1단계~4단계"로 임시 지정함.
 * 디자인/기획에서 실제 이름(예: 아기/유아/청소년/성체 같은) 정해지면 이 enum의 name만 바꾸면 됨.
 *
 * ⚠️ 코스튬(옷장) 착용: 팀 확정(2026-08-17) - 단계 상관없이 처음부터(1단계) 착용 가능.
 * (1차 기획의 "7일차부터"는 폐기됨)
 */
public enum GrowthStage {
    STAGE_1(0, 49),
    STAGE_2(50, 119),
    STAGE_3(120, 199),
    STAGE_4(200, 300);

    private final int minPoints;
    private final int maxPoints;

    GrowthStage(int minPoints, int maxPoints) {
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
    }

    public static GrowthStage fromPoints(int points) {
        int clamped = Math.max(0, Math.min(300, points));
        for (GrowthStage stage : values()) {
            if (clamped >= stage.minPoints && clamped <= stage.maxPoints) {
                return stage;
            }
        }
        return STAGE_4; // 안전망 (이론상 도달 안 함)
    }

    /**
     * 코스튬 착용 가능 여부. 팀 확정(2026-08-17): 단계 상관없이 처음부터(1단계, 경험치 0부터) 착용 가능.
     */
    public boolean canWearCostume() {
        return true;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }
}
