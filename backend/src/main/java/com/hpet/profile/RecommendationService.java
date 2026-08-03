package com.hpet.profile;

import com.hpet.common.exception.HealthProfileNotFoundException;
import com.hpet.domain.profile.Gender;
import com.hpet.domain.profile.HealthProfile;
import com.hpet.domain.profile.HealthProfileRepository;
import com.hpet.domain.supplement.Supplement;
import com.hpet.domain.supplement.SupplementRepository;
import com.hpet.profile.dto.RecommendationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2 - 2-4. AI 맞춤 추천 API.
 * "AI"라고 부르지만 MVP에서는 규칙 기반(rule-based)으로 구현한다.
 * 나중에 실제 모델 기반 추천으로 바꾸더라도, 인터페이스(추천 결과 반환 형태)는
 * 그대로 두고 이 클래스 내부 로직만 교체하면 되도록 분리해뒀다.
 *
 * 완료 기준(문서 2-4): 프로필 값이 다르면 추천 결과도 달라져야 한다.
 * 아래 규칙들이 그 조건을 만족한다 - 나이/성별/메모 키워드에 따라 결과가 달라진다.
 */
@Service
public class RecommendationService {

    private final HealthProfileRepository healthProfileRepository;
    private final SupplementRepository supplementRepository;

    public RecommendationService(HealthProfileRepository healthProfileRepository,
                                  SupplementRepository supplementRepository) {
        this.healthProfileRepository = healthProfileRepository;
        this.supplementRepository = supplementRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> recommend(Long userId) {
        HealthProfile profile = healthProfileRepository.findByUserId(userId)
                .orElseThrow(HealthProfileNotFoundException::new);

        // key: 영양제 이름, value: 추천 사유 (여러 규칙에 걸리면 마지막 사유로 덮어쓰지 않고 첫 사유 유지)
        Map<String, String> recommendedNameToReason = new LinkedHashMap<>();

        applyAgeRule(profile, recommendedNameToReason);
        applyGenderRule(profile, recommendedNameToReason);
        applyMemoKeywordRules(profile, recommendedNameToReason);
        applyDefaultRule(recommendedNameToReason);

        return toResponses(recommendedNameToReason);
    }

    private void applyAgeRule(HealthProfile profile, Map<String, String> result) {
        LocalDate birthDate = profile.getBirthDate();
        if (birthDate == null) {
            return;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age >= 40) {
            result.putIfAbsent("칼슘", "40세 이상은 골밀도 관리를 위해 칼슘을 추천합니다.");
            result.putIfAbsent("오메가3", "40세 이상은 혈행 관리를 위해 오메가3를 추천합니다.");
        }
    }

    private void applyGenderRule(HealthProfile profile, Map<String, String> result) {
        if (profile.getGender() == Gender.FEMALE) {
            result.putIfAbsent("철분", "여성은 철분 보충이 권장됩니다.");
        }
    }

    private void applyMemoKeywordRules(HealthProfile profile, Map<String, String> result) {
        String memo = profile.getMemo();
        if (memo == null || memo.isBlank()) {
            return;
        }
        String normalized = memo.replaceAll("\\s", "");

        if (containsAny(normalized, "피로", "수면", "불면")) {
            result.putIfAbsent("마그네슘", "메모에서 피로/수면 관련 키워드가 감지되어 추천합니다.");
            result.putIfAbsent("멜라토닌", "메모에서 피로/수면 관련 키워드가 감지되어 추천합니다.");
        }
        if (containsAny(normalized, "탈모", "모발", "손톱")) {
            result.putIfAbsent("비오틴", "메모에서 모발/손톱 관련 키워드가 감지되어 추천합니다.");
        }
        if (containsAny(normalized, "소화", "장건강", "변비")) {
            result.putIfAbsent("유산균", "메모에서 소화/장건강 관련 키워드가 감지되어 추천합니다.");
        }
        if (containsAny(normalized, "자세", "거북목", "허리")) {
            result.putIfAbsent("칼슘", "메모에서 자세/체형 관련 키워드가 감지되어 추천합니다.");
        }
    }

    private void applyDefaultRule(Map<String, String> result) {
        // 아무 규칙에도 안 걸리면 최소 하나는 추천되도록 기본값을 넣는다.
        if (result.isEmpty()) {
            result.put("비타민", "기초 영양 보충을 위해 종합 비타민을 추천합니다.");
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<RecommendationResponse> toResponses(Map<String, String> nameToReason) {
        List<RecommendationResponse> responses = new ArrayList<>();
        for (Map.Entry<String, String> entry : nameToReason.entrySet()) {
            supplementRepository.findByName(entry.getKey()).ifPresent(supplement ->
                    responses.add(new RecommendationResponse(supplement.getId(), supplement.getName(), entry.getValue())));
        }
        return responses;
    }
}
