package com.hpet.vision;

/**
 * AI 비전 판정 결과. success/reason만 있으면 충분해서 record로 최소화했다.
 */
public record VisionJudgement(boolean success, String reason) {
}
