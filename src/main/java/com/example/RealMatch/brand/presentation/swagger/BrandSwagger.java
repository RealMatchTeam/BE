package com.example.RealMatch.brand.presentation.swagger;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.RealMatch.brand.presentation.dto.request.BrandCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCreateResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignSliceResponse;
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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Brand", description = "브랜드 API")
public interface BrandSwagger {

    @Operation(summary = "브랜드 생성", description = "새로운 브랜드를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    CustomResponse<BrandCreateResponseDto> createBrand(
            @RequestBody(description = "생성할 브랜드 정보", required = true,
                    content = @Content(schema = @Schema(implementation = BrandCreateRequestDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"brandName\": \"비플레인\",\n" +
                                    "  \"industryType\": \"BEAUTY\",\n" +
                                    "  \"logoUrl\": \"https://cdn.your-service.com/brands/beplain/logo.png\",\n" +
                                    "  \"simpleIntro\": \"천연 유래 성분으로 민감 피부를 위한 저자극 스킨케어 브랜드\",\n" +
                                    "  \"detailIntro\": \"티끌없는 순수 히알루론산™으로 피부속부터 촉촉한 #수분세럼\\n민감성 피부도 부담 없이 사용할 수 있는 저자극·천연재료 기반의 뷰티 브랜드입니다.\",\n" +
                                    "  \"homepageUrl\": \"https://www.beplain.co.kr\",\n" +
                                    "  \"brandCategory\": [\"스킨케어\", \"메이크업\"],\n" +
                                    "  \"brandSkinCareTag\": {\n" +
                                    "    \"skinType\": [\"건성\", \"지성\", \"복합성\"],\n" +
                                    "    \"mainFunction\": [\"수분/보습\", \"진정\"]\n" +
                                    "  },\n" +
                                    "  \"brandMakeUpTag\": {\n" +
                                    "    \"skinType\": [\"건성\", \"민감성\"],\n" +
                                    "    \"brandMakeUpStyle\": [\"내추럴\", \"글로우\"]\n" +
                                    "  }\n" +
                                    "}")))
            BrandCreateRequestDto requestDto
    );

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

    @Operation(
            summary = "브랜드의 캠페인 조회 API by 박지영",
            description = """
                    브랜드의 캠페인을 조회합니다.   
                    
                    마지막으로 조회된 캠페인의 id를 cursor로 사용하는 페이징 방식입니다.   
                    - 최초 조회 시 cursor 없이 요청합니다.     
                    - 이후 조회 시 응답으로 받은 nextCursor 값을 cursor로 전달합니다.    
                    - 정렬 조건 : UPCOMING /RECRUITING → 모집 시작 날짜 기준, CLOSED → 모집 끝나는 날짜 기준   
                    
                    캠페인 모집 상태는 다음 중 하나로 응답됩니다.   
                    - UPCOMING   : 모집 예정   
                    - RECRUITING : 모집 중   
                    - CLOSED     : 완료 (모집 마감) 
                    """
    )
    @GetMapping("/{brandId}/campaigns")
    CustomResponse<BrandCampaignSliceResponse> getBrandCampaigns(
            @Parameter(description = "브랜드 ID", example = "1")
            @PathVariable Long brandId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    );
}
