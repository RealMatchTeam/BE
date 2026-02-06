package com.example.RealMatch.user.presentation.swagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
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

    @Operation(
            summary = "마이페이지 프로필 카드 조회 API",
            description = "로그인한 사용자의 프로필 카드 정보와 매칭검사 결과를 조회합니다"
    )
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
            description = "로그인한 사용자의 특성 정보를 조회합니다."
    )
    CustomResponse<MyFeatureResponseDto> getMyFeature(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "내 특성 수정 API By 고경수",
            description = """
                    로그인한 사용자의 특성(뷰티/패션/콘텐츠)을 부분 수정(PATCH)합니다.
                    
                    요청 규칙 (PATCH / merge)
                    - 요청 Body는 MatchRequestDto 형태로 받습니다. (tagId 기반)
                    - 요청에 포함된 필드만 변경되고, 포함되지 않은 필드는 기존 값이 유지됩니다.
                    
                    처리 흐름
                    1) 현재 활성 UserMatchingDetail(삭제되지 않은 상태)을 조회합니다.
                    2) 기존(UserMatchingDetail)을 MatchRequestDto로 복원합니다.
                    3) 복원한 DTO와 요청 DTO를 merge하여 최종 MatchRequestDto(완성본)를 생성합니다.
                    4) 최종 DTO로 매칭을 재실행합니다. (matchService.match)
                       - MatchService 내부에서 기존 UserMatchingDetail은 deprecated 처리되고
                         새로운 UserMatchingDetail이 생성/저장됩니다.
                       - 매칭 결과(userType/creatorType)는 새로운 UserMatchingDetail에 저장됩니다.
                       - 브랜드/캠페인 매칭 히스토리도 최신 결과로 갱신됩니다.
                    
                    - 태그 값은 반드시 "정수 tagId"로 전달되어야 합니다.
                    - 본 API는 결과 응답을 반환하지 않으며(200 OK / null),
                      갱신된 creatorType은 프로필 카드/매칭 결과 조회 API에서 확인합니다.
                    """
    )
    @PatchMapping("/me/feature")
    CustomResponse<Void> updateMyFeature(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchRequestDto request
    );
}
