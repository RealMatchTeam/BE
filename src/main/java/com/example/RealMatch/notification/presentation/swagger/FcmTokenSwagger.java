package com.example.RealMatch.notification.presentation.swagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.notification.presentation.dto.request.FcmTokenRegisterRequest;
import com.example.RealMatch.notification.presentation.dto.request.FcmTokenRemoveRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface FcmTokenSwagger {

    @Operation(
            summary = "FCM 토큰 등록 API by 여채현",
            description = """
                    사용자의 FCM 디바이스 토큰을 등록합니다.
                    
                    - 동일 토큰이 이미 존재하면 소유자를 현재 유저로 재할당합니다.
                    - 사용자당 여러 디바이스 토큰이 허용됩니다.
                    - 웹 브라우저 푸시 알림을 위해 로그인 시 호출합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 등록 성공")
    })
    CustomResponse<Void> registerToken(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FcmTokenRegisterRequest request
    );

    @Operation(
            summary = "FCM 토큰 삭제 API by 여채현",
            description = """
                    사용자의 FCM 디바이스 토큰을 삭제합니다.
                    
                    - 로그아웃 시 호출하여 해당 디바이스에 푸시가 가지 않도록 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 삭제 성공")
    })
    CustomResponse<Void> removeToken(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FcmTokenRemoveRequest request
    );
}
