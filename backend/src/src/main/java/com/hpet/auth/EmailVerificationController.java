package com.hpet.auth;

import com.hpet.auth.dto.ConfirmVerificationEmailRequest;
import com.hpet.auth.dto.SendVerificationEmailRequest;
import com.hpet.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - Email Verification", description = "이메일 인증 (데모: 실제 발송 대신 서버 콘솔 로그에 코드 출력)")
@RestController
@RequestMapping("/api/auth/email-verification")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @Operation(summary = "인증코드 재발송 - 서버 콘솔 로그에서 코드 확인")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody SendVerificationEmailRequest request) {
        emailVerificationService.sendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "인증코드 확인")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(@Valid @RequestBody ConfirmVerificationEmailRequest request) {
        emailVerificationService.confirmVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
