package com.example.RealMatch.global.auth.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.auth.details.CustomOAuth2User;
import com.example.RealMatch.global.auth.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    // 테스트용
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            log.info("OAuth2 authentication successful for user: {}", oAuth2User.getUser().getId());

            String accessToken = jwtProvider.createAccessToken(oAuth2User.getUser());
            String refreshToken = jwtProvider.createRefreshToken(oAuth2User.getUser());

            log.debug("Tokens created - Access token length: {}", accessToken.length());

            String redirectUrl = String.format("%s/login/success?accessToken=%s&refreshToken=%s",
                    frontendUrl,
                    URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                    URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
            );

            log.info("Redirecting to: {}", frontendUrl + "/login/success");
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error during OAuth2 success handling", e);
            response.sendRedirect(frontendUrl + "/login/error?message=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
