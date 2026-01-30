package com.example.RealMatch.user.presentation.swagger;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
import com.example.RealMatch.user.presentation.dto.response.MyCampaignDetailResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyCampaignListResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyEditInfoResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyFeatureResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyLoginResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
            description = "찜한 브랜드 또는 캠페인 목록을 조회합니다. GUEST 권한이거나 매칭 테스트 기록이 없으면 접근할 수 없습니다. (하드코딩)"
    )
    CustomResponse<MyScrapResponseDto> getMyScrap(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "찜 타입 (brand 또는 campaign)", required = true, example = "brand")
            @RequestParam String type,
            @Parameter(description = "정렬 기준", example = "matchingRate")
            @RequestParam(required = false, defaultValue = "matchingRate") String sort
    );

    @Operation(
            summary = "회원 정보 변경 기본 조회 API By 고경수",
            description = "내 정보 수정에 필요한 정보를 조회합니다."
    )
    CustomResponse<MyEditInfoResponseDto> getMyEditInfo(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "회원정보 변경 API By 고경수", description = "사용자의 닉네임, 주소, 상세주소를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    CustomResponse<Void> updateMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MyEditInfoRequestDto request
    );

    @Operation(
            summary = "소셜 로그인 연동 정보 조회 API By 고경수",
            description = "현재 사용자의 소셜 로그인 연동 상태를 조회합니다. 카카오, 네이버, 구글 계정의 연동 여부를 확인할 수 있습니다."
    )
    CustomResponse<MyLoginResponseDto> getSocialLoginInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "내 특성 조회 API By 고경수",
            description = "로그인한 사용자의 특성 정보를 조회합니다. (하드코딩)"
    )
    CustomResponse<MyFeatureResponseDto> getMyFeature(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "내가 진행한 캠페인 목록 조회 API By 고경수",
            description = "로그인한 사용자가 지원(Apply)하거나 제안(Proposal)한 캠페인 목록을 조회합니다. " +
                    "1페이지는 최대 3개, 2페이지부터는 4개씩 조회됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "프로필 카드 접근 권한 없음 (GUEST 또는 매칭 테스트 미완료)"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    CustomResponse<Page<MyCampaignListResponseDto.CampaignItem>> getMyCampaigns(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작, 0페이지=3개, 1페이지부터=4개씩)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "정렬 기준 (latest=최신순, oldest=오래된순)", example = "latest")
            @RequestParam(required = false, defaultValue = "latest") String sort
    );

    @Operation(
            summary = "내가 진행한 캠페인 상세 조회 API By 고경수",
            description = "내가 지원하거나 제안한 특정 캠페인의 상세 정보를 조회합니다. " +
                    "applicationId 형식: 'apply-{uuid}' (지원) 또는 'proposal-{uuid}' (제안)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "해당 캠페인에 접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "지원/제안 내역을 찾을 수 없음")
    })
    CustomResponse<MyCampaignDetailResponseDto> getMyCampaignDetail(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(
                    description = "지원/제안 ID (형식: 'apply-{uuid}' 또는 'proposal-{uuid}')",
                    example = "apply-550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String applicationId
    );
}
