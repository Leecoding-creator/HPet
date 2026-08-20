package com.hpet.profile;

import com.hpet.common.ApiResponse;
import com.hpet.profile.dto.HealthProfileRequest;
import com.hpet.profile.dto.HealthProfileResponse;
import com.hpet.profile.dto.RecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Health Profile", description = "건강 프로필 (Phase 2)")
@RestController
@RequestMapping("/api/profile")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;
    private final RecommendationService recommendationService;

    public HealthProfileController(HealthProfileService healthProfileService,
                                    RecommendationService recommendationService) {
        this.healthProfileService = healthProfileService;
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "건강 프로필 저장 (upsert)")
    @PostMapping
    public ResponseEntity<ApiResponse<HealthProfileResponse>> save(
            @AuthenticationPrincipal Long userId, @RequestBody HealthProfileRequest request) {
        HealthProfileResponse response = healthProfileService.upsert(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 건강 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> getMine(@AuthenticationPrincipal Long userId) {
        HealthProfileResponse response = healthProfileService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "AI 맞춤 영양제 추천 (규칙 기반) - 건강 프로필 등록 후 호출")
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> recommend(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.recommend(userId)));
    }
}
