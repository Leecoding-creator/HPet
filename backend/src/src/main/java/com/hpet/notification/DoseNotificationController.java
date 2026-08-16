package com.hpet.notification;

import com.hpet.common.ApiResponse;
import com.hpet.notification.dto.DoseNotificationRequest;
import com.hpet.notification.dto.DoseNotificationResponse;
import com.hpet.notification.dto.DoseNotificationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Dose Notification", description = "복용 알림 시간 CRUD (Phase 5 - 5-1)")
@RestController
@RequestMapping("/api/notifications")
public class DoseNotificationController {

    private final DoseNotificationService doseNotificationService;

    public DoseNotificationController(DoseNotificationService doseNotificationService) {
        this.doseNotificationService = doseNotificationService;
    }

    @Operation(summary = "알림 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<DoseNotificationResponse>> create(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody DoseNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(doseNotificationService.create(userId, request)));
    }

    @Operation(summary = "내 알림 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DoseNotificationResponse>>> getMine(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(doseNotificationService.getMine(userId)));
    }

    @Operation(summary = "알림 수정 (시간/on-off)")
    @PutMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<DoseNotificationResponse>> update(
            @AuthenticationPrincipal Long userId, @PathVariable Long notificationId,
            @Valid @RequestBody DoseNotificationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(doseNotificationService.update(userId, notificationId, request)));
    }

    @Operation(summary = "알림 삭제")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userId, @PathVariable Long notificationId) {
        doseNotificationService.delete(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
