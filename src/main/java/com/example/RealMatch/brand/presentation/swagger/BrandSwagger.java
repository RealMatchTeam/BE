package com.example.RealMatch.brand.presentation.swagger;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandLikeResponseDto;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Brand", description = "브랜드 API")
public interface BrandSwagger {

    @Operation(summary = "브랜드 상세 조회", description = "브랜드 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<java.util.List<BrandDetailResponseDto>> getBrandDetail(
            @Parameter(description = "조회할 브랜드의 ID", required = true) @PathVariable Long brandId
    );

    @Operation(summary = "브랜드 좋아요 토글", description = "브랜드 ID로 좋아요를 추가하거나 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<List<BrandLikeResponseDto>> likeBrand(
            @Parameter(description = "좋아요 토글할 브랜드의 ID", required = true) @PathVariable Long brandId
    );

    @Operation(summary = "브랜드 필터 옵션 조회", description = "브랜드 필터링에 사용될 옵션들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CustomResponse<List<BrandFilterResponseDto>> getBrandFilters();
}
