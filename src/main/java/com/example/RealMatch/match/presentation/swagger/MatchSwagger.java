package com.example.RealMatch.match.presentation.swagger;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.domain.entity.enums.CategoryType;
import com.example.RealMatch.match.domain.entity.enums.SortType;
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
                                                      "userId": "1",
                                                      "brandId": "101",
                                                      "sex": "여성",
                                                      "age": 25,
                                                      "height": 165,
                                                      "weight": 52,
                                                      "size": {
                                                        "upper": 55,
                                                        "bottom": 26
                                                      },
                                                      "beauty": {
                                                        "interests": ["스킨케어", "메이크업"],
                                                        "functions": ["보습", "미백"],
                                                        "skinType": "복합성",
                                                        "skinTone": "웜톤",
                                                        "makeupStyle": "내추럴"
                                                      },
                                                      "fashion": {
                                                        "styles": ["미니멀", "캐주얼"],
                                                        "items": ["원피스", "블라우스"],
                                                        "preferredBrands": ["자라", "유니클로"]
                                                      },
                                                      "sns": {
                                                        "url": "https://www.instagram.com/beauty_creator",
                                                        "mainAudience": {
                                                          "sex": ["여성"],
                                                          "age": ["20대", "30대"]
                                                        },
                                                        "contentStyle": {
                                                          "avgVideoLength": "30초~1분",
                                                          "avgViews": "50000",
                                                          "format": "릴스",
                                                          "type": "리뷰",
                                                          "contributionLevel": "높음",
                                                          "usageCoverage": "전체"
                                                        }
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ) MatchRequestDto requestDto);

    @Operation(summary = "매칭 브랜드 목록 조회",
            description = """
                    사용자 ID를 기반으로 매칭률이 높은 브랜드 목록을 조회합니다.
                    정렬 옵션: MATCH_SCORE(매칭률 순), POPULARITY(인기순), NEWEST(신규순)
                    카테고리 필터: ALL(전체), FASHION(패션), BEAUTY(뷰티)
                    태그 필터: 뷰티/패션 관련 태그로 필터링
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공")
    })
    CustomResponse<MatchBrandResponseDto> getMatchingBrands(
            @Parameter(description = "사용자 ID") @PathVariable String userId,
            @Parameter(description = "정렬 기준 (MATCH_SCORE, POPULARITY, NEWEST)") @RequestParam(defaultValue = "MATCH_SCORE") SortType sortBy,
            @Parameter(description = "카테고리 필터 (ALL, FASHION, BEAUTY)") @RequestParam(defaultValue = "ALL") CategoryType category,
            @Parameter(description = "태그 필터 (예: 스킨케어, 미니멀)") @RequestParam(required = false) List<String> tags);

    @Operation(summary = "매칭 캠페인 목록 조회",
            description = """
                    사용자 ID를 기반으로 매칭률이 높은 캠페인 목록을 조회합니다.
                    정렬 옵션: MATCH_SCORE(매칭률 순), POPULARITY(인기순), NEWEST(신규순)
                    카테고리 필터: ALL(전체), FASHION(패션), BEAUTY(뷰티)
                    태그 필터: 뷰티/패션 관련 태그로 필터링
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캠페인 목록 조회 성공")
    })
    CustomResponse<MatchCampaignResponseDto> getMatchingCampaigns(
            @Parameter(description = "사용자 ID") @PathVariable String userId,
            @Parameter(description = "정렬 기준 (MATCH_SCORE, POPULARITY, NEWEST)") @RequestParam(defaultValue = "MATCH_SCORE") SortType sortBy,
            @Parameter(description = "카테고리 필터 (ALL, FASHION, BEAUTY)") @RequestParam(defaultValue = "ALL") CategoryType category,
            @Parameter(description = "태그 필터 (예: 스킨케어, 미니멀)") @RequestParam(required = false) List<String> tags);
}
