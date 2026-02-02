package com.example.RealMatch.brand.presentation.controller;

import com.example.RealMatch.brand.application.service.BrandService;
import com.example.RealMatch.brand.presentation.dto.request.BrandCreateRequestDto;
import com.example.RealMatch.brand.presentation.dto.request.BrandUpdateRequestDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandCreateResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandFilterResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandLikeResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.BrandListResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductDetailResponseDto;
import com.example.RealMatch.brand.presentation.dto.response.SponsorProductListResponseDto;
import com.example.RealMatch.brand.presentation.swagger.BrandSwagger;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController implements BrandSwagger {

    private final BrandService brandService;

    @Override
    @GetMapping("/{brandId}")
    public CustomResponse<java.util.List<BrandDetailResponseDto>> getBrandDetail(@PathVariable Long brandId) {
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, Collections.singletonList(brandService.getBrandDetail(brandId)));
    }

    @Override
    @PostMapping("/{brandId}/like")
    public CustomResponse<List<BrandLikeResponseDto>> likeBrand(@PathVariable Long brandId) {
        Boolean isLiked = brandService.likeBrand(brandId);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, Collections.singletonList(new BrandLikeResponseDto(isLiked)));
    }

    @Override
    @GetMapping("/filters")
    public CustomResponse<List<BrandFilterResponseDto>> getBrandFilters() {
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, Collections.singletonList(brandService.getBrandFilters()));
    }

    @Override
    @GetMapping("/{brandId}/sponsor-products/{productId}")
    public CustomResponse<SponsorProductDetailResponseDto> getSponsorProductDetail(@PathVariable Long brandId, @PathVariable Long productId) {
        return CustomResponse.ok(brandService.getSponsorProductDetail(brandId, productId));
    }

    @Override
    @GetMapping("/{brandId}/sponsor-products")
    public CustomResponse<List<SponsorProductListResponseDto>> getSponsorProducts(
            @PathVariable Long brandId
    ) {
        return CustomResponse.ok(brandService.getSponsorProducts(brandId));
    }

    @PostMapping
    public CustomResponse<BrandCreateResponseDto> createBrand(@RequestBody BrandCreateRequestDto requestDto) {
        BrandCreateResponseDto responseDto = brandService.createBrand(requestDto);
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, responseDto);
    }

    @PatchMapping("/{brandId}")
    public ResponseEntity<Void> updateBrand(@PathVariable Long brandId, @RequestBody BrandUpdateRequestDto requestDto) {
        brandService.updateBrand(brandId, requestDto);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long brandId) {
        brandService.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping
    public CustomResponse<List<BrandListResponseDto>> getAllBrands() {
        return CustomResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, brandService.getAllBrands());
    }

    // 임시 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<Long> getBrandIdByUserId(@PathVariable Long userId) {
        Long brandId = brandService.getBrandIdByUserId(userId);
        return ResponseEntity.ok(brandId);
    }
}
