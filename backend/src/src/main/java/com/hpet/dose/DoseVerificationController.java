package com.hpet.dose;

import com.hpet.common.ApiResponse;
import com.hpet.dose.dto.DoseVerificationResponse;
import com.hpet.dose.dto.DoseVerificationStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Dose Verification", description = "사진 기반 복용 인증 + 포션 지급 (Phase 5, 경진 담당)")
@RestController
@RequestMapping("/api/dose-verification")
public class DoseVerificationController {

    private final DoseVerificationService doseVerificationService;

    public DoseVerificationController(DoseVerificationService doseVerificationService) {
        this.doseVerificationService = doseVerificationService;
    }

    @Operation(summary = "복용 사진 업로드 → AI 판정 → 성공 시 DoseRecord 반영 + 포션 지급까지 한 번에 처리")
    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DoseVerificationResponse>> verifyPhoto(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long userSupplementId,
            @RequestParam MultipartFile image) {
        DoseVerificationResponse response = doseVerificationService.verifyPhoto(userId, userSupplementId, image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "오늘 이 영양제의 인증 상태 조회")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<DoseVerificationStatusResponse>> getStatus(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long userSupplementId) {
        return ResponseEntity.ok(ApiResponse.success(doseVerificationService.getStatus(userId, userSupplementId)));
    }
}
