package com.example.RealMatch.oauth.swagger;

import org.springframework.web.bind.annotation.RequestBody;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.oauth.dto.OAuthTokenResponse;
import com.example.RealMatch.oauth.dto.request.SignupCompleteRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Authentication", description = "사용자 인증 API")
public interface AuthSwagger {

    @Operation(
            summary = "추가 정보 회원가입 API By 고경수",
            description = """
                    소셜 로그인 후, 닉네임, 생년월일, 역할(CREATOR/BRAND), 약관 동의 등 필수 정보를 입력하여
                    최종적으로 서비스를 이용할 수 있는 권한을 부여하는 API입니다.
                    응답 본문에는 새 액세스 토큰만 담기고, 새 리프레시 토큰은 HttpOnly 쿠키(refresh_token)로 내려갑니다.
                    """)
    CustomResponse<OAuthTokenResponse> signup(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody SignupCompleteRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse httpResponse
    );

    @Operation(
            summary = "액세스 토큰 재발급 API By 고경수",
            description = """
                    HttpOnly 쿠키(refresh_token)에 담긴 리프레시 토큰으로 새 액세스 토큰을 발급받습니다.
                    요청 시 credentials(withCredentials)을 포함해야 쿠키가 전송됩니다.
                    리프레시 토큰은 매 호출마다 로테이션되어 새 쿠키로 교체되며, 이전 토큰은 즉시 무효화됩니다.
                    소셜 로그인 콜백 직후 및 앱 최초 로드 시 이 API 로 액세스 토큰을 받아 메모리에만 보관하세요.
                    """)
    CustomResponse<OAuthTokenResponse> refresh(
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse httpResponse
    );

    @Operation(
            summary = "로그아웃 API By 고경수",
            description = "서버에 저장된 리프레시 토큰을 폐기하고 refresh_token 쿠키를 삭제합니다. 액세스 토큰 없이도 호출 가능합니다.")
    CustomResponse<Void> logout(
            @Parameter(hidden = true) HttpServletRequest httpRequest,
            @Parameter(hidden = true) HttpServletResponse httpResponse
    );
}
