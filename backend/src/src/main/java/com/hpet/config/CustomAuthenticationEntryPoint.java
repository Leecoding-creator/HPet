package com.hpet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpet.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 토큰 없이(또는 잘못된 토큰으로) 인증 필요 API를 호출했을 때,
 * Spring Security 기본 403 대신 우리 공통 ApiResponse 포맷으로 401을 내려준다.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.error("UNAUTHORIZED", "인증이 필요합니다.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
