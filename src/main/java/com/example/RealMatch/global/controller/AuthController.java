package com.example.RealMatch.global.controller;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.oauth.dto.OAuthTokenResponse;
import com.example.RealMatch.global.oauth.dto.request.SignupCompleteRequest;
import com.example.RealMatch.global.oauth.service.AuthService;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.example.RealMatch.global.presentation.swagger.AuthSwagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthSwagger {

    private final AuthService authService;

    @PostMapping("/signup")
    public CustomResponse<OAuthTokenResponse> signup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SignupCompleteRequest request
        ) {
            OAuthTokenResponse response = authService.completeSignup(userDetails.getUserId(), request);
            return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
        }

    @PostMapping("/refresh")
    public CustomResponse<OAuthTokenResponse> refresh(@RequestHeader("RefreshToken") String refreshToken) { // Authorization -> RefreshToken으로 변경
        OAuthTokenResponse response = authService.refreshAccessToken(refreshToken);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response);
    }
}
