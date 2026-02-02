package com.example.RealMatch.match.presentation.swagger;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.domain.entity.enums.BrandSortType;
import com.example.RealMatch.match.domain.entity.enums.CampaignSortType;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Match", description = "크리에이터-브랜드 매칭 API")
@RequestMapping("/api/v1/matches")
public interface MatchSwagger {

    @Operation(summary = "크리에이터 매칭 분석",
            description = """
                    크리에이터 정보를 기반으로 매칭 분석 결과와 추천 브랜드 목록을 반환합니다.
                    userType, typeTag, highMatchingBrandList를 포함합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 분석 성공")
    })
    CustomResponse<MatchResponseDto> match(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(
                    description = "크리에이터 매칭 요청 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MatchRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "뷰티 크리에이터 예시",
                                            summary = "뷰티 중심 크리에이터",
                                            value = """
                                                    {
                                                      "beauty": {
                                                        "interestStyleTags": [1, 2],
                                                        "prefferedFunctionTags": [6, 7],
                                                        "skinTypeTags": 12,
                                                        "skinToneTags": 13,
                                                        "makeupStyleTags": 2
                                                      },
                                                      "fashion": {
                                                        "interestStyleTags": [16, 17],
                                                        "preferredItemTags": [22, 23],
                                                        "preferredBrandTags": [27, 28],
                                                        "heightTag": 72,
                                                        "weightTypeTag": 94,
                                                        "topSizeTag": 108,
                                                        "bottomSizeTag": 178
                                                      },
                                                      "content": {
                                                        "sns": {
                                                          "url": "https://www.instagram.com/vivi",
                                                          "mainAudience": {
                                                            "genderTags": [221, 222],
                                                            "ageTags": [223, 224]
                                                          },
                                                          "averageAudience": {
                                                            "videoLengthTags": [228, 229],
                                                            "videoViewsTags": [232, 233]
                                                          }
                                                        },
                                                        "typeTags": [236, 237],
                                                        "toneTags": [245, 246],
                                                        "prefferedInvolvementTags": [251, 252],
                                                        "prefferedCoverageTags": [255, 256]
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ) MatchRequestDto requestDto);

    @Operation(summary = "매칭 브랜드 목록 조회",
            description = """
                    JWT 토큰의 사용자 ID를 기반으로 매칭률이 높은 브랜드 목록을 조회합니다.
                    정렬 옵션: MATCH_SCORE(매칭률 순), POPULARITY(인기순), NEWEST(신규순)
                    카테고리 필터: ALL(전체), FASHION(패션), BEAUTY(뷰티)
                    태그 필터: 뷰티/패션 관련 태그로 필터링
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공")
    })
    CustomResponse<MatchBrandResponseDto> getMatchingBrands(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "정렬 기준 (MATCH_SCORE, POPULARITY, NEWEST)") @RequestParam(defaultValue = "MATCH_SCORE") BrandSortType sortBy,
            @Parameter(description = "카테고리 필터 (ALL, FASHION, BEAUTY)") @RequestParam(defaultValue = "ALL") CategoryType category,
            @Parameter(description = "태그 필터 (예: 스킨케어, 미니멀)") @RequestParam(required = false) List<String> tags);

    @Operation(summary = "매칭 캠페인 목록 조회 및 검색",
            description = """
                    JWT 토큰의 사용자 ID를 기반으로 매칭 캠페인 목록을 검색하거나 매칭 캠페인 목록을 조회합니다.
                    
                    **검색**: keyword를 입력하면 캠페인명(title)만 검색합니다. (브랜드명, 설명 등은 검색 대상 제외)
                    
                    **정렬 옵션**:
                    - MATCH_SCORE: 매칭률 순 (동점 시 인기순 우선)
                    - POPULARITY: 인기 순 (좋아요 수)
                    - REWARD_AMOUNT: 금액 순 (원고료 높은 순)
                    - D_DAY: 마감 순 (마감 임박순)
                    
                    **카테고리 필터**: ALL(전체), FASHION(패션), BEAUTY(뷰티)
                    
                    **페이지네이션**: page(0부터 시작), size(기본 20)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캠페인 목록 조회 및 검색 성공")
    })
    CustomResponse<MatchCampaignResponseDto> getMatchingCampaigns(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "캠페인명 검색어 (캠페인 title만 검색)") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 (MATCH_SCORE, POPULARITY, REWARD_AMOUNT, D_DAY)") @RequestParam(defaultValue = "MATCH_SCORE") CampaignSortType sortBy,
            @Parameter(description = "카테고리 필터 (ALL, FASHION, BEAUTY)") @RequestParam(defaultValue = "ALL") CategoryType category,
            @Parameter(description = "태그 필터 (예: 스킨케어, 미니멀)") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (기본 20, 최대 50)") @RequestParam(defaultValue = "20") int size);
}
