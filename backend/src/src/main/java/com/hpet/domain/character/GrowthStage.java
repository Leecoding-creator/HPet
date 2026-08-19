package com.hpet.domain.character;

/**
 * 회의 1-3 확정: 누적 성장 일수에 따른 성장 단계.
 * 성장치(포션)는 Phase 5에서 지급되지만, 단계 계산 로직은 미리 만들어둔다.
 */
public enum GrowthStage {
    BABY(1, 2),          // 아기
    TODDLER(3, 6),        // 유아
    CHILD(7, 13),         // 어린이 (코스튬 착용 가능 시작)
    TEEN(14, 20),         // 청소년
    ADULT(21, 30),        // 성체
    GROWN(31, Integer.MAX_VALUE); // 성장 완료

    private final int minDay;
    private final int maxDay;

    GrowthStage(int minDay, int maxDay) {
        this.minDay = minDay;
        this.maxDay = maxDay;
    }

    /**
     * 누적 성장 일수(growthDays)로부터 현재 단계를 계산한다.
     * 0일차(아직 하루도 다 못 채움)는 BABY 단계로 취급한다.
     */
    public static GrowthStage fromDays(int days) {
        int effectiveDays = Math.max(days, 1);
        for (GrowthStage stage : values()) {
            if (effectiveDays >= stage.minDay && effectiveDays <= stage.maxDay) {
                return stage;
            }
        }
        return GROWN;
    }

    /**
     * 회의 1-2 확정: 7일차(어린이 단계)부터 코스튬 착용 가능.
     */
    public boolean canWearCostume() {
        return this.ordinal() >= CHILD.ordinal();
    }
}
