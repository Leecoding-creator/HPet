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

/**
 * 팀 확정(2026-08-19, 준호님): 영양제별로 따로 인증하던 방식에서, 오늘 등록한 영양제를 전부
 * 한 사진에 모아서 한 번에 인증(AI가 알약 개수를 셈)하는 방식으로 변경됨.
 * 그래서 더 이상 userSupplementId를 지정하지 않는다 - 오늘 단위로만 인증한다.
 */
@Tag(name = "Dose Verification", description = "사진 기반 복용 인증(전체·알약 개수 세기) + 포션 지급 (Phase 5, 경진 담당)")
@RestController
@RequestMapping("/api/dose-verification")
public class DoseVerificationController {

    private final DoseVerificationService doseVerificationService;

    public DoseVerificationController(DoseVerificationService doseVerificationService) {
        this.doseVerificationService = doseVerificationService;
    }

    @Operation(summary = "오늘 먹을 영양제를 한 사진에 모아 촬영 -> AI가 알약 개수 세서 판정 -> 포션 지급까지 한 번에 처리 (여러 번 나눠 찍기 가능)")
    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DoseVerificationResponse>> verifyPhoto(
            @AuthenticationPrincipal Long userId,
            @RequestParam MultipartFile image) {
        DoseVerificationResponse response = doseVerificationService.verifyPhoto(userId, image);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "오늘 인증 상태 조회 (몇 개 등록 중 몇 개 인증됐는지)")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<DoseVerificationStatusResponse>> getStatus(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(doseVerificationService.getStatus(userId)));
    }
}
