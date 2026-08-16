package com.hpet.user;

import com.hpet.common.ApiResponse;
import com.hpet.common.exception.InvalidTokenException;
import com.hpet.domain.user.User;
import com.hpet.domain.user.UserRepository;
import com.hpet.user.dto.MyProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT 인증이 실제로 걸려있는지 눈으로 확인할 수 있는 데모용 API.
 * Swagger에서 Authorize에 accessToken을 넣고 호출해보면 됨.
 */
@Tag(name = "User", description = "로그인한 사용자 정보")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "내 정보 조회 (인증 필요)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> me(@AuthenticationPrincipal Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 사용자입니다."));

        MyProfileResponse response = new MyProfileResponse(
                user.getId(), user.getEmail(), user.getProvider().name(), user.isEmailVerified(), user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
