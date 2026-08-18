package com.hpet.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    // 탈퇴(소프트 삭제)한 계정은 "존재하지 않는 것"처럼 취급하기 위한 메서드들.
    // 회원가입 중복 체크, 로그인 시 반드시 이걸 써야 "탈퇴 후 재가입 시 이메일 중복" 문제가 안 생긴다.
    boolean existsByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
