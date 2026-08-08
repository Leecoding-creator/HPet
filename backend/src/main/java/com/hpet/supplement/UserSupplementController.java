package com.hpet.supplement;

import com.hpet.common.ApiResponse;
import com.hpet.supplement.dto.RegisterSupplementsRequest;
import com.hpet.supplement.dto.UserSupplementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Supplement", description = "내가 등록한 영양제 (Phase 2)")
@RestController
@RequestMapping("/api/users/me/supplements")
public class UserSupplementController {

    private final UserSupplementService userSupplementService;

    public UserSupplementController(UserSupplementService userSupplementService) {
        this.userSupplementService = userSupplementService;
    }

    @Operation(summary = "영양제 등록 - 처음 등록 시 매핑되는 캐릭터가 자동 배정됨")
    @PostMapping
    public ResponseEntity<ApiResponse<List<UserSupplementResponse>>> register(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody RegisterSupplementsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userSupplementService.register(userId, request)));
    }

    @Operation(summary = "내가 등록한 영양제 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSupplementResponse>>> getMine(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userSupplementService.getMyList(userId)));
    }

    @Operation(summary = "영양제 등록 취소 - 관련 복용기록/알림이 있으면 삭제 불가")
    @DeleteMapping("/{userSupplementId}")
    public ResponseEntity<ApiResponse<Void>> unregister(
            @AuthenticationPrincipal Long userId, @PathVariable Long userSupplementId) {
        userSupplementService.unregister(userId, userSupplementId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
