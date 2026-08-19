package com.hpet.domain.user;

/**
 * 회의 1-1 확정: 지금은 EMAIL만 사용하지만, 나중에 소셜 로그인을 붙일 수 있도록
 * User 엔티티에 provider 필드는 남겨둔다.
 */
public enum AuthProvider {
    EMAIL
}
