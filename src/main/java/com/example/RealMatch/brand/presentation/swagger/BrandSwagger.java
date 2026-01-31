package com.example.RealMatch.brand.presentation.swagger;

import com.example.RealMatch.brand.presentation.dto.request.BrandUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandLikeResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandListResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductListResponseDto;
import com.example.RealMatch.global.presentation.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

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

    @Operation(summary = "협찬 가능 제품 상세 조회", description = "브랜드의 특정 협찬 가능 제품 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<SponsorProductDetailResponseDto> getSponsorProductDetail(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long brandId,
            @Parameter(description = "제품 ID", required = true) @PathVariable Long productId
    );

    @Operation(summary = "브랜드 협찬 가능 제품 리스트 조회", description = "특정 브랜드의 협찬 가능 제품 목록을 조회합니다.")
    CustomResponse<List<SponsorProductListResponseDto>> getSponsorProducts(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long brandId
    );

    @Operation(summary = "브랜드 정보 수정", description = "특정 브랜드의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    ResponseEntity<Void> updateBrand(
            @Parameter(description = "수정할 브랜드의 ID", required = true) @PathVariable Long brandId,
            @RequestBody(description = "수정할 브랜드 정보") BrandUpdateRequestDto requestDto
    );

    @Operation(summary = "브랜드 삭제", description = "브랜드 ID로 브랜드를 삭제합니다. (소프트 삭제)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    ResponseEntity<Void> deleteBrand(
            @Parameter(description = "삭제할 브랜드의 ID", required = true) @PathVariable Long brandId
    );

    @Operation(summary = "브랜드 전체 목록 조회", description = "등록된 모든 브랜드의 리스트를 반환합니다.")
    CustomResponse<List<BrandListResponseDto>> getAllBrands();
}
