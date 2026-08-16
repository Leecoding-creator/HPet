package com.hpet.auth;

import com.hpet.auth.dto.*;
import com.hpet.common.exception.DuplicateEmailException;
import com.hpet.common.exception.InvalidCredentialsException;
import com.hpet.common.exception.InvalidTokenException;
import com.hpet.domain.user.User;
import com.hpet.domain.user.UserRepository;
import com.hpet.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 1 핵심 로직.
 * - 1-2 회원가입 (이메일 중복 체크, BCrypt 해시)
 * - 1-4 로그인 (JWT access/refresh 발급)
 * - 1-6 리프레시 토큰 재발급 + 로그아웃
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                        EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        log.info("New user signed up: id={}, email={}", saved.getId(), saved.getEmail());

        // 회원가입 직후 바로 인증코드를 발급한다 (콘솔 로그로 출력됨 - 데모 스코프).
        emailVerificationService.sendVerificationCode(saved);

        return new SignupResponse(saved.getId(), saved.getEmail());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());
        user.updateRefreshToken(refreshToken);

        log.info("User logged in: id={}", user.getId());
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.isValid(refreshToken) || !"REFRESH".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 사용자입니다."));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new InvalidTokenException("폐기되었거나 일치하지 않는 리프레시 토큰입니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());
        user.updateRefreshToken(newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 사용자입니다."));
        user.updateRefreshToken(null);
        log.info("User logged out: id={}", userId);
    }
}
