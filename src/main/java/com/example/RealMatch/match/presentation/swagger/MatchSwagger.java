package com.example.RealMatch.match.presentation.swagger;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.match.presentation.dto.request.MatchRequestDto;
import com.example.RealMatch.match.presentation.dto.response.MatchBrandResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchCampaignResponseDto;
import com.example.RealMatch.match.presentation.dto.response.MatchResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Match", description = "크리에이터-브랜드 매칭 API")
@RequestMapping("/api/v1/matches")
public interface MatchSwagger {

    @Operation(summary = "크리에이터 매칭 분석",
            description = """
                    크리에이터 정보를 기반으로 매칭 분석 결과와 추천 브랜드/캠페인 목록을 반환합니다.
                    creatorAnalysis, highMatchingBrandList, highMatchingCampaignList를 포함합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 분석 성공")
    })
    CustomResponse<MatchResponseDto> matchBrand(@RequestBody MatchRequestDto requestDto);

    @Operation(summary = "매칭 브랜드 목록 조회",
            description = """
                    사용자 ID를 기반으로 매칭률이 높은 브랜드 목록을 조회합니다.
                    브랜드 정보와 매칭률, 태그 등을 포함합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공")
    })
    CustomResponse<MatchBrandResponseDto> getMatchingBrands(
            @Parameter(description = "사용자 ID") @PathVariable String userId);

    @Operation(summary = "매칭 캠페인 목록 조회",
            description = """
                    사용자 ID를 기반으로 매칭률이 높은 캠페인 목록을 조회합니다.
                    캠페인 정보, 원고료, 모집 현황 등을 포함합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "캠페인 목록 조회 성공")
    })
    CustomResponse<MatchCampaignResponseDto> getMatchingCampaigns(
            @Parameter(description = "사용자 ID") @PathVariable String userId);
}
