package com.example.RealMatch.brand.presentation.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.brand.application.service.BrandService;
import com.example.RealMatch.brand.presentation.dto.request.BrandBeautyCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandBeautyUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandFashionCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandFashionUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.response.*;
import com.example.RealMatch.brand.presentation.swagger.BrandSwagger;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController implements BrandSwagger {

    private final BrandService brandService;

    // ******** //
    // 브랜드 조회 //
    // ******** //
    @Override
    @GetMapping("/{brandId}")
    public CustomResponse<java.util.List<BrandDetailResponseDto>> getBrandDetail(
        @PathVariable Long brandId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        BrandDetailResponseDto result = brandService.getBrandDetail(brandId, currentUserId);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, Collections.singletonList(result));
    }

    @Override
    @PostMapping("/{brandId}/like")
    public CustomResponse<List<BrandLikeResponseDto>> likeBrand(
        @PathVariable Long brandId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        Boolean isLiked = brandService.likeBrand(brandId, currentUserId);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, Collections.singletonList(new BrandLikeResponseDto(isLiked)));
    }

    @Override
    @GetMapping("/filters")
    public CustomResponse<List<BrandFilterResponseDto>> getBrandFilters() {

        BrandFilterResponseDto result = brandService.getBrandFilters();
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, Collections.singletonList(result));
    }

    @Override
    @GetMapping("/{brandId}/sponsor-products/{productId}")
    public CustomResponse<SponsorProductDetailResponseDto> getSponsorProductDetail(
        @PathVariable Long brandId, 
        @PathVariable Long productId
    ) {
        SponsorProductDetailResponseDto result = brandService.getSponsorProductDetail(brandId, productId);
        return CustomResponse.ok(result);
    }

    @Override
    @GetMapping("/{brandId}/sponsor-products")
    public CustomResponse<List<SponsorProductListResponseDto>> getSponsorProducts(
            @PathVariable Long brandId
    ) {
        return CustomResponse.ok(brandService.getSponsorProducts(brandId));
    }

    @GetMapping("/{brandId}/summary")
    public CustomResponse<BrandSimpleDetailResponse> getBrandSummary(
            @PathVariable Long brandId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 유저 정보는 security context에서 가져온 userDetails.getUserId() 사용
        BrandSimpleDetailResponse result = brandService.getSimpleBrandDetail(brandId, userDetails.getUserId());

        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, result);
    }

    // ******** //
    // 브랜드 생성 //
    // ******** //
    @Override
    @PostMapping("/beauty")
    public CustomResponse<BrandCreateResponseDto> createBeautyBrand(
        @RequestBody BrandBeautyCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        BrandCreateResponseDto responseDto = brandService.createBeautyBrand(requestDto, currentUserId);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, responseDto);
    }

    @Override
    @PostMapping("/fashion")
    public CustomResponse<BrandCreateResponseDto> createFashionBrand(
        @RequestBody BrandFashionCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        BrandCreateResponseDto responseDto = brandService.createFashionBrand(requestDto, currentUserId);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, responseDto);
    }

    // *********** //
    // 브랜드 업데이트 //
    // *********** //
    @Override
    @PatchMapping("/beauty/{brandId}")
    public ResponseEntity<Void> updateBeautyBrand(
        @PathVariable Long brandId,
        @RequestBody BrandBeautyUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        brandService.updateBeautyBrand(brandId, requestDto, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/fashion/{brandId}")
    public ResponseEntity<Void> updateFashionBrand(
        @PathVariable Long brandId,
        @RequestBody BrandFashionUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        brandService.updateFashionBrand(brandId, requestDto, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // ******** //
    // 브랜드 삭제 //
    // ******** //
    @Override
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteBrand(
        @PathVariable Long brandId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = principal.getUserId();
        brandService.deleteBrand(brandId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping
    public CustomResponse<Page<BrandListResponseDto>> getAllBrands(@PageableDefault(size = 10) Pageable pageable) {
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, brandService.getAllBrands(pageable));
    }

    // 임시 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<Long> getBrandIdByUserId(@PathVariable Long userId) {
        Long brandId = brandService.getBrandIdByUserId(userId);
        return ResponseEntity.ok(brandId);
    }
}
