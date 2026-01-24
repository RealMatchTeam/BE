package com.example.RealMatch.brand.presentation.controller;

import com.example.RealMatch.brand.application.BrandService;
import com.example.RealMatch.brand.presentation.dto.response.BeautyFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailViewResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandLikeViewResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandListResponseDto;
import com.example.RealMatch.global.presentation.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Brand", description = "브랜드 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "브랜드 목록 조회", description = "검색, 정렬, 필터링 조건을 사용하여 브랜드 목록을 조회합니다.<br>" +
            "동적인 상세 필터는 `subCategories`, `skinTypes` 등의 파라미터를 직접 사용하여 전달합니다. (예: `...&skinTypes=DRY,OILY&functions=SOOTHING`)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "브랜드 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BAD_REQUEST: 잘못된 도메인 요청", content = @Content(schema = @Schema(implementation = CustomResponse.class, example = "{\"isSuccess\":false, \"code\":\"BAD_REQUEST\", \"message\":\"잘못된 요청입니다\", \"result\":\"유효하지 않은 도메인입니다.\"}")))
    })
    @Parameters({
            @Parameter(name = "domain", description = "도메인 구분 (BEAUTY 또는 FASHION)", required = true, in = ParameterIn.QUERY, example = "BEAUTY"),
            @Parameter(name = "sort", description = "정렬 기준 (MATCH_RATE, POPULARITY, NEWEST)", in = ParameterIn.QUERY, example = "MATCH_RATE"),
            @Parameter(name = "keyword", description = "검색어", in = ParameterIn.QUERY, example = "라운드랩"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, example = "0"),
            @Parameter(name = "size", description = "페이지 당 개수", in = ParameterIn.QUERY, example = "10"),
            // --- 상세 필터 (실제 파라미터) ---
            @Parameter(name = "subCategories", description = "[뷰티] 카테고리 필터 (다중 선택 가능)", in = ParameterIn.QUERY, schema = @Schema(type = "array", implementation = String.class)),
            @Parameter(name = "functions", description = "[뷰티] 기능 필터 (다중 선택 가능)", in = ParameterIn.QUERY, schema = @Schema(type = "array", implementation = String.class)),
            @Parameter(name = "skinTypes", description = "[뷰티] 피부타입 필터 (다중 선택 가능)", in = ParameterIn.QUERY, schema = @Schema(type = "array", implementation = String.class)),
            @Parameter(name = "makeupStyles", description = "[뷰티] 메이크업 스타일 필터 (다중 선택 가능)", in = ParameterIn.QUERY, schema = @Schema(type = "array", implementation = String.class)),
            @Parameter(name = "brandTypes", description = "[패션] 브랜드 종류 필터 (다중 선택 가능)", in = ParameterIn.QUERY, schema = @Schema(type = "array", implementation = String.class)),
            @Parameter(name = "styles", description = "[패션] 스타일 필터 (다중 선택 가능)", in = ParameterIn.QUERY, schema = @Schema(type = "array", implementation = String.class))
    })
    @GetMapping
    public CustomResponse<BrandListResponseDto> getBrands(
            @RequestParam String domain,
            @RequestParam(required = false, defaultValue = "MATCH_RATE") String sort,
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true) @RequestParam Map<String, List<String>> filters,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        return CustomResponse.ok(brandService.getBrands(domain, sort, keyword, filters, pageable));
    }

    @Operation(summary = "필터 옵션 메타데이터 조회 (구)", description = "정렬 및 상세 필터에 사용될 선택지 목록을 조회합니다. (Beauty 필터는 /filters/beauty 로 분리됨)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "필터 옵션 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BAD_REQUEST: 잘못된 도메인 요청", content = @Content(schema = @Schema(implementation = CustomResponse.class, example = "{\"isSuccess\":false, \"code\":\"BAD_REQUEST\", \"message\":\"잘못된 요청입니다\", \"result\":\"유효하지 않은 도메인입니다.\"}")))
    })
    @GetMapping("/filters")
    public CustomResponse<BrandFilterResponseDto> getBrandFilters(
            @Parameter(description = "필터 옵션을 조회할 도메인 (FASHION 등)", required = true, example = "FASHION") @RequestParam String domain) {
        return CustomResponse.ok(brandService.getBrandFilters(domain));
    }

    @Operation(summary = "뷰티 필터 옵션 조회", description = "뷰티 도메인의 상세 필터 옵션 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "뷰티 필터 옵션 조회 성공")
    })
    @GetMapping("/filters/beauty")
    public CustomResponse<List<BeautyFilterResponseDto>> getBeautyFilters() {
        return CustomResponse.ok(brandService.getBeautyFilters());
    }

    @Operation(summary = "브랜드 상세 정보 조회", description = "특정 브랜드의 상세 정보, 캠페인, 협찬 제품 등을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "브랜드 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NOT_FOUND: 해당 브랜드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = CustomResponse.class, example = "{\"isSuccess\":false, \"code\":\"NOT_FOUND\", \"message\":\"리소스를 찾을 수 없습니다.\", \"result\":\"해당 브랜드를 찾을 수 없습니다.\"}")))
    })
    @GetMapping("/{brandId}")
    public CustomResponse<BrandDetailViewResponseDto> getBrandDetail(
            @Parameter(description = "조회할 브랜드의 ID", required = true, example = "1") @PathVariable Long brandId) {
        return CustomResponse.ok(brandService.getBrandDetail(brandId));
    }

    @Operation(summary = "브랜드 좋아요 토글", description = "특정 브랜드의 좋아요 상태를 변경(토글)합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "브랜드 좋아요 토글 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NOT_FOUND: 해당 브랜드를 찾을 수 없음", content = @Content(schema = @Schema(implementation = CustomResponse.class, example = "{\"isSuccess\":false, \"code\":\"NOT_FOUND\", \"message\":\"리소스를 찾을 수 없습니다.\", \"result\":\"해당 브랜드를 찾을 수 없습니다.\"}")))
    })
    @PostMapping("/{brandId}/like")
    public CustomResponse<List<BrandLikeViewResponseDto>> toggleBrandLike(
            @Parameter(description = "좋아요를 토글할 브랜드의 ID", required = true, example = "1") @PathVariable Long brandId) {
        return CustomResponse.ok(brandService.toggleBrandLike(brandId));
    }
}
