package com.hpet.auth;

import com.hpet.auth.dto.PasswordResetConfirmRequest;
import com.hpet.auth.dto.PasswordResetRequest;
import com.hpet.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - Password Reset", description = "비밀번호 재설정 (데모: 실제 발송 대신 서버 콘솔 로그에 토큰 출력)")
@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "재설정 요청 - 서버 콘솔 로그에서 토큰 확인")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> request(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "재설정 확인 - 토큰 + 새 비밀번호로 변경")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
