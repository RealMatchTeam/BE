package com.example.RealMatch.oauth.controller;

import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.example.RealMatch.oauth.code.OAuthErrorCode;
import com.example.RealMatch.oauth.dto.IssuedTokens;
import com.example.RealMatch.oauth.dto.OAuthTokenResponse;
import com.example.RealMatch.oauth.dto.request.SignupCompleteRequest;
import com.example.RealMatch.oauth.service.AuthService;
import com.example.RealMatch.oauth.swagger.AuthSwagger;
import com.example.RealMatch.oauth.token.RefreshTokenCookieManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthSwagger {

    private final AuthService authService;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    @PostMapping("/signup")
    public CustomResponse<OAuthTokenResponse> signup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SignupCompleteRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Optional<String> currentRefreshToken = refreshTokenCookieManager.read(httpRequest);
        IssuedTokens tokens = authService.completeSignup(
                userDetails.getUserId(), userDetails.getProviderId(), request, currentRefreshToken);

        refreshTokenCookieManager.attach(httpResponse, tokens.refreshToken());
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, tokens.toResponse());
    }

    @PostMapping("/refresh")
    public CustomResponse<OAuthTokenResponse> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = refreshTokenCookieManager.read(httpRequest)
                .orElseThrow(() -> new CustomException(OAuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        IssuedTokens tokens;
        try {
            tokens = authService.refresh(refreshToken);
        } catch (CustomException e) {
            // 재사용/만료된 토큰은 클라이언트 쿠키에서도 지워 재시도 루프를 막는다
            refreshTokenCookieManager.expire(httpResponse);
            throw e;
        }

        refreshTokenCookieManager.attach(httpResponse, tokens.refreshToken());
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, tokens.toResponse());
    }

    @PostMapping("/logout")
    public CustomResponse<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        refreshTokenCookieManager.read(httpRequest).ifPresent(authService::revoke);
        refreshTokenCookieManager.expire(httpResponse);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null);
    }
}
