package com.hpet.agreement;

import com.hpet.agreement.dto.AgreementRequest;
import com.hpet.agreement.dto.AgreementResponse;
import com.hpet.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Agreement", description = "약관/건강정보 동의 이력 (Phase 1 - 1-9)")
@Validated
@RestController
@RequestMapping("/api/users/me/agreements")
public class AgreementController {

    private final AgreementService agreementService;

    public AgreementController(AgreementService agreementService) {
        this.agreementService = agreementService;
    }

    @Operation(summary = "약관 동의 제출 - 서비스이용약관/개인정보처리방침/건강정보수집 3종 필수")
    @PostMapping
    public ResponseEntity<ApiResponse<List<AgreementResponse>>> submit(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody @NotEmpty List<AgreementRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(agreementService.submit(userId, requests)));
    }

    @Operation(summary = "내 동의 이력 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AgreementResponse>>> getHistory(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(agreementService.getHistory(userId)));
    }
}
