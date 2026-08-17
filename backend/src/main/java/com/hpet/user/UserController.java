package com.hpet.user;

import com.hpet.auth.AuthService;
import com.hpet.common.ApiResponse;
import com.hpet.user.dto.MyProfileResponse;
import com.hpet.user.dto.UpdateNicknameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 로그인한 사용자 정보 조회/수정 + 회원 탈퇴.
 * Swagger에서 Authorize에 accessToken을 넣고 호출해보면 됨.
 */
@Tag(name = "User", description = "로그인한 사용자 정보")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(summary = "내 정보 조회 (인증 필요)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> me(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(userId)));
    }

    @Operation(summary = "닉네임 수정")
    @PutMapping("/me/nickname")
    public ResponseEntity<ApiResponse<MyProfileResponse>> updateNickname(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateNicknameRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateNickname(userId, request)));
    }

    @Operation(summary = "회원 탈퇴 (소프트 삭제) - 탈퇴 후 같은 이메일로 재가입 가능")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal Long userId) {
        authService.withdraw(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
