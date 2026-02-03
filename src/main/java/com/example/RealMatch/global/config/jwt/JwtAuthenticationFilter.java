package com.example.RealMatch.global.config.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // JWT 검증을 건너뛸 경로들
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login",
            "/oauth2/",
            "/login/oauth2/",
            "/oauth/callback",
            "/v3/api-docs/**",
            "/swagger-ui",
            "/swagger-resources"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // JWT 검증을 건너뛸 경로인지 확인
        if (shouldNotFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 마스터 JWT 체크 (개발/테스트용)
        if (jwtProvider.isMasterJwt(token)) {
            CustomUserDetails masterUser = new CustomUserDetails(
                    0L,              // 마스터 사용자 ID
                    "master",        // providerId
                    "ADMIN",          // 관리자 권한
                    "master@admin.com"
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            masterUser.getUserId().toString(), // Principal을 userId(String)로 설정
                            null,
                            masterUser.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtProvider.getUserId(token);
        String providerId = jwtProvider.getProviderId(token);
        String role = jwtProvider.getRole(token);
        String email = jwtProvider.getEmail(token);

        CustomUserDetails userDetails =
                new CustomUserDetails(userId, providerId, role, email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails.getUserId().toString(), // Principal을 userId(String)로 설정
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean shouldNotFilter(String requestURI) {
        // 더 정확한 경로 비교를 위해 startsWith 대신 equals 또는 정규식 사용을 고려할 수 있습니다.
        return EXCLUDED_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }
}
