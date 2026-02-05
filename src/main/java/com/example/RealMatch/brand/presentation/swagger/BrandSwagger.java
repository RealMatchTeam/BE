package com.example.RealMatch.brand.presentation.swagger;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.RealMatch.brand.presentation.dto.request.BrandBeautyCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandBeautyUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandFashionCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandFashionUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCreateResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandLikeResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandListResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductListResponseDto;
import com.example.RealMatch.global.presentation.CustomResponse;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Brand", description = "브랜드 API")
public interface BrandSwagger {

    @Operation(summary = "뷰티 브랜드 생성 by 이예림", description = "새로운 뷰티 브랜드를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<BrandCreateResponseDto> createBeautyBrand(
            @RequestBody(description = "생성할 뷰티 브랜드 정보", required = true,
                    content = @Content(schema = @Schema(implementation = BrandBeautyCreateRequestDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"brandName\": \"비플레인\",\n" +
                                    "  \"logoUrl\": \"https://cdn.your-service.com/brands/beplain/logo.png\",\n" +
                                    "  \"simpleIntro\": \"천연 유래 성분으로 민감 피부를 위한 저자극 스킨케어 브랜드\",\n" +
                                    "  \"detailIntro\": \"민감성 피부도 부담 없이 사용할 수 있는 저자극·천연재료 기반의 뷰티 브랜드입니다.\",\n" +
                                    "  \"homepageUrl\": \"https://www.beplain.co.kr\",\n" +
                                    "  \"brandDescriptionTags\": [\"저자극\", \"천연성분\"],\n" +
                                    "  \"brandTags\": {\n" +
                                    "    \"interestStyle\": [1, 2, 3],\n" +
                                    "    \"interestFunction\": [1, 2],\n" +
                                    "    \"skinType\": [1, 2],\n" +
                                    "    \"makeupStyle\": [1, 3]\n" +
                                    "  }\n" +
                                    "}")))
            BrandBeautyCreateRequestDto requestDto,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "패션 브랜드 생성 by 이예림", description = "새로운 패션 브랜드를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<BrandCreateResponseDto> createFashionBrand(
            @RequestBody(description = "생성할 패션 브랜드 정보", required = true,
                    content = @Content(schema = @Schema(implementation = BrandFashionCreateRequestDto.class)))
            BrandFashionCreateRequestDto requestDto,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "브랜드 상세 조회 by 이예림", description = "브랜드 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<java.util.List<BrandDetailResponseDto>> getBrandDetail(
            @Parameter(description = "조회할 브랜드의 ID", required = true) @PathVariable Long brandId,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "브랜드 좋아요 토글 by 이예림", description = "브랜드 ID로 좋아요를 추가하거나 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<List<BrandLikeResponseDto>> likeBrand(
            @Parameter(description = "좋아요 토글할 브랜드의 ID", required = true) @PathVariable Long brandId,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "브랜드 필터 옵션 조회 by 이예림", description = "브랜드 필터링에 사용될 옵션들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    CustomResponse<List<BrandFilterResponseDto>> getBrandFilters();

    @Operation(summary = "협찬 가능 제품 상세 조회 by 이예림", description = "브랜드의 특정 협찬 가능 제품 상세 정보를 조회합니다.")
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

    @Operation(summary = "브랜드 협찬 가능 제품 리스트 조회 by 이예림", description = "특정 브랜드의 협찬 가능 제품 목록을 조회합니다.")
    CustomResponse<List<SponsorProductListResponseDto>> getSponsorProducts(
            @Parameter(description = "브랜드 ID", required = true) @PathVariable Long brandId
    );

    @Operation(summary = "뷰티 브랜드 정보 수정 by 이예림", description = "뷰티 브랜드의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    ResponseEntity<Void> updateBeautyBrand(
            @Parameter(description = "수정할 브랜드의 ID", required = true) @PathVariable Long brandId,
            @RequestBody(description = "수정할 뷰티 브랜드 정보",
                    content = @Content(schema = @Schema(implementation = BrandBeautyUpdateRequestDto.class)))
            BrandBeautyUpdateRequestDto requestDto,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "패션 브랜드 정보 수정 by 이예림", description = "패션 브랜드의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    ResponseEntity<Void> updateFashionBrand(
            @Parameter(description = "수정할 브랜드의 ID", required = true) @PathVariable Long brandId,
            @RequestBody(description = "수정할 패션 브랜드 정보",
                    content = @Content(schema = @Schema(implementation = BrandFashionUpdateRequestDto.class)))
            BrandFashionUpdateRequestDto requestDto,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "브랜드 삭제 by 이예림", description = "브랜드 ID로 브랜드를 삭제합니다. (소프트 삭제)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    ResponseEntity<Void> deleteBrand(
            @Parameter(description = "삭제할 브랜드의 ID", required = true) @PathVariable Long brandId,
            @Parameter(hidden = true) CustomUserDetails principal
    );

    @Operation(summary = "브랜드 전체 목록 조회 (페이징) by 이예림", description = "등록된 모든 브랜드의 리스트를 페이징하여 반환합니다.")
    CustomResponse<Page<BrandListResponseDto>> getAllBrands(
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "유저 ID로 브랜드 ID 조회 by 이예림", description = "유저 ID에 해당하는 브랜드 ID를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 또는 브랜드",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    ResponseEntity<Long> getBrandIdByUserId(
            @Parameter(description = "조회할 유저의 ID", required = true) @PathVariable Long userId
    );
}
