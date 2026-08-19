package com.hpet.vision;

/**
 * AI 비전 판정 결과.
 * pillCount: 영양제 인증(judge)에서만 의미 있음 - 사진에서 AI가 센 알약 개수 (자세 판정에서는 항상 0).
 */
public record VisionJudgement(boolean success, String reason, int pillCount) {
}
