package com.hpet.user;

import com.hpet.common.exception.InvalidTokenException;
import com.hpet.domain.user.User;
import com.hpet.domain.user.UserRepository;
import com.hpet.user.dto.MyProfileResponse;
import com.hpet.user.dto.UpdateNicknameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        User user = findActiveUser(userId);
        return toResponse(user);
    }

    /**
     * 닉네임 수정. 팀 리스트 반영: "프로필 닉네임 수정 시 상단바 등에 즉시 반영 안 되는 문제"는
     * 프론트 상태관리 이슈라 백엔드는 그냥 저장/조회만 정확히 해주면 된다.
     */
    @Transactional
    public MyProfileResponse updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = findActiveUser(userId);
        user.updateNickname(request.getNickname());
        log.info("Nickname updated: userId={}, nickname={}", userId, request.getNickname());
        return toResponse(user);
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 사용자입니다."));
        if (user.isDeleted()) {
            throw new InvalidTokenException("탈퇴한 사용자입니다.");
        }
        return user;
    }

    private MyProfileResponse toResponse(User user) {
        return new MyProfileResponse(
                user.getId(), user.getEmail(), user.getNickname(), user.getProvider().name(),
                user.isEmailVerified(), user.getCreatedAt());
    }
}
