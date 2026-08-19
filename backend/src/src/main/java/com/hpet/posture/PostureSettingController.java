package com.hpet.posture;

import com.hpet.common.ApiResponse;
import com.hpet.posture.dto.PostureSettingRequest;
import com.hpet.posture.dto.PostureSettingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Posture", description = "자세 교정 (Phase 4)")
@RestController
@RequestMapping("/api/posture-settings")
public class PostureSettingController {

    private final PostureSettingService postureSettingService;

    public PostureSettingController(PostureSettingService postureSettingService) {
        this.postureSettingService = postureSettingService;
    }

    @Operation(summary = "자세 감지 활성 시간대 설정 저장 (upsert)")
    @PostMapping
    public ResponseEntity<ApiResponse<PostureSettingResponse>> save(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody PostureSettingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postureSettingService.upsert(userId, request)));
    }

    @Operation(summary = "현재 자세 감지 설정 조회 - 저장된 설정이 없으면 기본값(enabled=false) 반환")
    @GetMapping
    public ResponseEntity<ApiResponse<PostureSettingResponse>> getMine(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(postureSettingService.getMine(userId)));
    }
}
