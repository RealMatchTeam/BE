package com.example.RealMatch.global.config.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.RealMatch.user.domain.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository users;
    private final Environment environment;

    // JWT 검증을 건너뛸 경로들
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login",
            "/oauth2",
            "/login/oauth2",
            "/oauth/callback",
            "/api/v1/ws",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/actuator",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/test"
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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT required");
            return;
        }

        String token = authHeader.substring(7);

        // 마스터 JWT 체크 (개발/테스트용)
        if (!environment.matchesProfiles("prod") && jwtProvider.isMasterJwt(token)) {
            CustomUserDetails masterUser = new CustomUserDetails(
                    0L,              // 마스터 사용자 ID
                    "master",        // providerId
                    "ADMIN",          // 관리자 권한
                    "master@admin.com"
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            masterUser,
                            null,
                            masterUser.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        io.jsonwebtoken.Claims claims;
        Long userId;
        try {
            claims = jwtProvider.getClaims(token);
            if (!"access".equals(claims.get("type", String.class))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token required");
                return;
            }
            userId = Long.valueOf(claims.getSubject());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
            return;
        }
        if (users.findById(userId).isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Inactive user");
            return;
        }
        String providerId = claims.get("providerId", String.class);
        String role = claims.get("role", String.class);
        String email = claims.get("email", String.class);

        CustomUserDetails userDetails =
                new CustomUserDetails(userId, providerId, role, email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
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
