package com.example.RealMatch.global.config.oauth;

import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User oAuth2User =
                (CustomOAuth2User) authentication.getPrincipal();

        Long userId = oAuth2User.getUserId();
        String provider = oAuth2User.getProvider();
        String role = oAuth2User.getRole();

        // 토큰 발급
        String accessToken = jwtProvider.createAccessToken(
                userId,
                provider,
                role
        );

        String refreshToken = jwtProvider.createRefreshToken(
                userId,
                provider,
                role
        );

        // 응답 데이터 구성
        OAuthTokenResponse tokenResponse =
                OAuthTokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

        // 공통 응답 래핑
        CustomResponse<OAuthTokenResponse> responseBody =
                CustomResponse.onSuccess(
                        GeneralSuccessCode.AUTHORIZED,
                        tokenResponse
                );

        // HTTP 응답
        response.setStatus(GeneralSuccessCode.AUTHORIZED.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}