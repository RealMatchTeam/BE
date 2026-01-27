package com.example.RealMatch.brand.presentation.controller;

import java.util.Collections;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.brand.application.BrandService;
import com.example.RealMatch.brand.presentation.dto.response.BrandDetailResponseDto;
import com.example.RealMatch.brand.presentation.swagger.BrandSwagger;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;

import lombok.RequiredArgsConstructor;

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
}
