package com.example.RealMatch.user.presentation.swagger;

import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "user", description = "유저 관련 API")
public interface UserSwagger {

    @Operation(summary = "마이페이지 메인 조회 API By 고경수", description = "로그인한 사용자의 마이페이지 정보를 조회합니다.")
    CustomResponse<MyPageResponseDto> getMyPage(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "마이페이지 프로필 카드 조회 API By 고경수", description = "로그인한 사용자의 마이페이지 프로필 카드 정보를 조회합니다. (하드코딩 - 프로필 카드 데이터 삽입 안했습니다!!)")
    CustomResponse<MyProfileCardResponseDto> getMyProfileCard(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "내 찜 목록 조회 API By 고경수",
            description = "찜한 브랜드 또는 캠페인 목록을 조회합니다. GUEST 권한이거나 매칭 테스트 기록이 없으면 접근할 수 없습니다. (하드코딩 - 캠페인 데이터 삽입 안했습니다!!)"
    )
    CustomResponse<MyScrapResponseDto> getMyScrap(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "찜 타입 (brand 또는 campaign)", required = true, example = "brand")
            @RequestParam String type,
            @Parameter(description = "정렬 기준", example = "matchingRate")
            @RequestParam(required = false, defaultValue = "matchingRate") String sort
    );
}
