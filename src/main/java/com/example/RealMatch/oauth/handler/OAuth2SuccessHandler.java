package com.example.RealMatch.oauth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.oauth.dto.CustomOAuth2User;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Value("${front.domain-url:http://localhost:8080}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User oAuth2User =
                (CustomOAuth2User) authentication.getPrincipal();

        Long userId = oAuth2User.getUserId();
        AuthProvider provider = oAuth2User.getProvider();
        String role = oAuth2User.getRole();
        String email = oAuth2User.getEmail();

        String accessToken = jwtProvider.createAccessToken(
                userId,
                provider.name(),
                role,  // ROLE_GUEST
                email
        );

        String refreshToken = jwtProvider.createRefreshToken(
                userId,
                provider.name(),
                role,  // ROLE_GUEST
                email
        );

        // provider별로 프론트엔드 콜백 경로 설정
        String callbackPath = getCallbackPath(provider);

        String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(callbackPath)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String getCallbackPath(AuthProvider provider) {
        return switch (provider) {
            case KAKAO -> "/auth/callback/kakao";
            case NAVER -> "/auth/callback/naver";
            case GOOGLE -> "/auth/callback/google";
        };
    }
}
