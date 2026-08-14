package com.hpet.auth;

import com.hpet.auth.dto.ConfirmVerificationEmailRequest;
import com.hpet.auth.dto.SendVerificationEmailRequest;
import com.hpet.common.email.EmailSender;
import com.hpet.common.exception.InvalidVerificationCodeException;
import com.hpet.common.exception.UserNotFoundException;
import com.hpet.domain.user.User;
import com.hpet.domain.user.UserRepository;
import com.hpet.domain.verification.EmailVerificationCode;
import com.hpet.domain.verification.EmailVerificationCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Phase 1 - 이메일 인증.
 * 실제 메일 발송 대신 ConsoleEmailSender로 콘솔에 인증코드를 찍는다 (해커톤 데모용).
 * 나중에 EmailSender 구현체만 실제 SMTP로 바꾸면 이 서비스 로직은 그대로 재사용 가능.
 */
@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int CODE_LENGTH = 6;
    private static final long EXPIRATION_MINUTES = 5;

    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final EmailSender emailSender;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(UserRepository userRepository,
                                     EmailVerificationCodeRepository codeRepository,
                                     EmailSender emailSender) {
        this.userRepository = userRepository;
        this.codeRepository = codeRepository;
        this.emailSender = emailSender;
    }

    @Transactional
    public void sendVerificationCode(SendVerificationEmailRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        sendVerificationCode(user);
    }

    @Transactional
    public void sendVerificationCode(User user) {
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        codeRepository.save(new EmailVerificationCode(user.getId(), code, expiresAt));

        emailSender.send(
                user.getEmail(),
                "[HPet] 이메일 인증코드",
                "인증코드: " + code + " (유효시간 " + EXPIRATION_MINUTES + "분)"
        );
        log.info("Verification code issued for userId={}", user.getId());
    }

    @Transactional
    public void confirmVerificationCode(ConfirmVerificationEmailRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        EmailVerificationCode verificationCode = codeRepository
                .findTopByUserIdAndCodeAndUsedFalseOrderByCreatedAtDesc(user.getId(), request.getCode())
                .orElseThrow(InvalidVerificationCodeException::new);

        if (verificationCode.isExpired()) {
            throw new InvalidVerificationCodeException();
        }

        verificationCode.markUsed();
        user.markEmailVerified();
        log.info("Email verified for userId={}", user.getId());
    }

    /**
     * 0~9 숫자로만 이루어진 6자리 인증코드를 생성한다 (예: "048213").
     * 맨 앞자리가 0이어도 상관없도록 문자열로 한 자리씩 채운다.
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
