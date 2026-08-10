package com.hpet.home.dto;

import com.hpet.character.dto.CharacterResponse;
import com.hpet.posture.dto.PostureSummaryResponse;

import java.util.List;

/**
 * Phase 3 - 홈 대시보드 요약. 오늘 복용 현황 + 캐릭터 상태 + 최근 자세 교정 현황을 한 번에 담는다.
 */
public class HomeSummaryResponse {

    private final DoseSummary doseSummary;
    private final CharacterResponse characterSummary; // 아직 배정된 캐릭터가 없으면 null
    private final PostureSummary postureSummary;

    public HomeSummaryResponse(DoseSummary doseSummary, CharacterResponse characterSummary, PostureSummary postureSummary) {
        this.doseSummary = doseSummary;
        this.characterSummary = characterSummary;
        this.postureSummary = postureSummary;
    }

    public DoseSummary getDoseSummary() { return doseSummary; }
    public CharacterResponse getCharacterSummary() { return characterSummary; }
    public PostureSummary getPostureSummary() { return postureSummary; }

    public static class DoseSummary {
        private final int totalSupplementCount;
        private final int completedCount;
        private final List<DoseItem> doseList;

        public DoseSummary(int totalSupplementCount, int completedCount, List<DoseItem> doseList) {
            this.totalSupplementCount = totalSupplementCount;
            this.completedCount = completedCount;
            this.doseList = doseList;
        }

        public int getTotalSupplementCount() { return totalSupplementCount; }
        public int getCompletedCount() { return completedCount; }
        public List<DoseItem> getDoseList() { return doseList; }
    }

    public static class DoseItem {
        private final Long userSupplementId;
        private final String supplementName;
        private final boolean completed;

        public DoseItem(Long userSupplementId, String supplementName, boolean completed) {
            this.userSupplementId = userSupplementId;
            this.supplementName = supplementName;
            this.completed = completed;
        }

        public Long getUserSupplementId() { return userSupplementId; }
        public String getSupplementName() { return supplementName; }
        public boolean isCompleted() { return completed; }
    }

    public static class PostureSummary {
        private final int todayCount;
        private final List<PostureSummaryResponse> recentDailyCounts;

        public PostureSummary(int todayCount, List<PostureSummaryResponse> recentDailyCounts) {
            this.todayCount = todayCount;
            this.recentDailyCounts = recentDailyCounts;
        }

        public int getTodayCount() { return todayCount; }
        public List<PostureSummaryResponse> getRecentDailyCounts() { return recentDailyCounts; }
    }
}
