package com.example.RealMatch.oauth.swagger;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.oauth.dto.OAuthTokenResponse;
import com.example.RealMatch.oauth.dto.request.SignupCompleteRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "사용자 인증 API")
public interface AuthSwagger {

    @Operation(summary = "추가 정보 회원가입 API By 고경수", description = "소셜 로그인 후, 닉네임, 생년월일, 역할(CREATOR/BRAND), 약관 동의 등 필수 정보를 입력하여 최종적으로 서비스를 이용할 수 있는 권한을 부여하는 API입니다.")

    CustomResponse<OAuthTokenResponse> signup(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody SignupCompleteRequest request
    );

    @Operation(summary = "액세스 토큰 재발급 API By 고경수", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    CustomResponse<OAuthTokenResponse> refresh(
            @Parameter(description = "Bearer {RefreshToken}", required = true)
            @RequestHeader("RefreshToken") String refreshToken
    );
}
