package com.example.RealMatch.oauth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.RealMatch.oauth.dto.CustomOAuth2User;
import com.example.RealMatch.oauth.dto.IssuedTokens;
import com.example.RealMatch.oauth.service.AuthService;
import com.example.RealMatch.oauth.token.RefreshTokenCookieManager;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

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

        IssuedTokens tokens = authService.issueTokens(userId, provider.name(), role, email);

        // 리프레시 토큰은 HttpOnly 쿠키로만 전달. URL 에는 어떤 토큰도 싣지 않는다
        // (브라우저 히스토리/서버 로그/Referer 로 새어나가고 localStorage 저장을 유도하기 때문).
        // 프론트 콜백 페이지는 POST /api/v1/auth/refresh 를 호출해 액세스 토큰을 메모리에 받는다.
        refreshTokenCookieManager.attach(response, tokens.refreshToken());

        String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(getCallbackPath(provider))
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
