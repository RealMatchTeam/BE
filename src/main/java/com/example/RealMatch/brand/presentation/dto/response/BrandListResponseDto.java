package com.example.RealMatch.brand.presentation.dto.response;

import com.example.RealMatch.brand.domain.entity.Brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandListResponseDto {
    private Long brandId;
    private String brandName;
    private String logoUrl;
    private String industryType; // 업종도 같이 보여주면 좋음

    public static BrandListResponseDto from(Brand brand) {
        return BrandListResponseDto.builder()
                .brandId(brand.getId())
                .brandName(brand.getBrandName())
                .logoUrl(brand.getLogoUrl())
                .industryType(brand.getIndustryType().name()) // 또는 .getDescription()
                .build();
    }
}
