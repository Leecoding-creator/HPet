package com.hpet.auth;

import com.hpet.auth.dto.PasswordResetConfirmRequest;
import com.hpet.auth.dto.PasswordResetRequest;
import com.hpet.common.email.EmailSender;
import com.hpet.common.exception.InvalidPasswordResetTokenException;
import com.hpet.common.exception.UserNotFoundException;
import com.hpet.domain.user.User;
import com.hpet.domain.user.UserRepository;
import com.hpet.domain.verification.PasswordResetToken;
import com.hpet.domain.verification.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Phase 1 - 비밀번호 재설정.
 * 실제 메일 발송 대신 ConsoleEmailSender로 콘솔에 재설정 토큰을 찍는다 (해커톤 데모용).
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final long EXPIRATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    public PasswordResetService(UserRepository userRepository,
                                 PasswordResetTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailSender emailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    @Transactional
    public void requestReset(PasswordResetRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        tokenRepository.save(new PasswordResetToken(user.getId(), token, expiresAt));

        emailSender.send(
                user.getEmail(),
                "[HPet] 비밀번호 재설정",
                "재설정 토큰: " + token + " (유효시간 " + EXPIRATION_MINUTES + "분)"
        );
        log.info("Password reset token issued for userId={}", user.getId());
    }

    @Transactional
    public void confirmReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (resetToken.isExpired()) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(UserNotFoundException::new);

        user.updatePasswordHash(passwordEncoder.encode(request.getNewPassword()));
        // 비밀번호가 바뀌었으니 기존 로그인 세션(refreshToken)은 전부 폐기해서 재로그인하게 만든다.
        user.updateRefreshToken(null);
        resetToken.markUsed();

        log.info("Password reset completed for userId={}", user.getId());
    }
}
