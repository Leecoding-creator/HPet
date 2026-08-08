package com.hpet.notification;

import com.hpet.common.ApiResponse;
import com.hpet.domain.notification.DeviceToken;
import com.hpet.domain.notification.DeviceTokenRepository;
import com.hpet.notification.dto.DeviceTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 5 - 5-2. 프론트가 로그인 후 발급받은 FCM 토큰을 여기로 등록해두면
 * 알림 스케줄러가 이 토큰들로 (지금은 콘솔 로그로) 푸시를 보낸다.
 */
@Tag(name = "Device Token", description = "푸시 알림용 기기 토큰 등록 (Phase 5 - 5-2)")
@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenController(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Operation(summary = "기기 토큰 등록 - 이미 등록된 토큰이면 그대로 무시")
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Void>> register(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody DeviceTokenRequest request) {
        if (!deviceTokenRepository.existsByToken(request.getToken())) {
            deviceTokenRepository.save(new DeviceToken(userId, request.getToken(), request.getPlatform()));
        }
        return ResponseEntity.ok(ApiResponse.success());
    }
}
