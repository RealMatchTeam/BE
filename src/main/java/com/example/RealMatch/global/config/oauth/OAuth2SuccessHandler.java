package com.example.RealMatch.global.config.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.config.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            // 토큰 생성
            String accessToken = jwtProvider.createAccessToken(
                    oAuth2User.getUser().getId(),
                    oAuth2User.getUser().getProvider(),
                    oAuth2User.getUser().getRole());
            String refreshToken = jwtProvider.createRefreshToken(
                    oAuth2User.getUser().getId(),
                    oAuth2User.getUser().getProvider(),
                    oAuth2User.getUser().getRole());

            // 테스트를 위해 백엔드 컨트롤러 주소로 직접 리다이렉트
            // 실제 연동 시 프론트엔드 서버 주소와 토큰을 받을 경로를 지정
            String redirectUrl = String.format("http://localhost:8080/api/login/success?accessToken=%s&refreshToken=%s",
                    URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                    URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
            );

            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error during OAuth2 success handling", e);
            response.sendRedirect("http://localhost:8080/api/test?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
