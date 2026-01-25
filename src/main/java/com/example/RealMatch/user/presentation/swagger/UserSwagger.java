package com.example.RealMatch.user.presentation.swagger;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "user", description = "유저 관련 API")
public interface UserSwagger {

    @Operation(summary = "마이페이지 메인 조회 API By 고경수", description = "로그인한 사용자의 마이페이지 정보를 조회합니다.")
    CustomResponse<MyPageResponseDto> getMyPage(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );
}
