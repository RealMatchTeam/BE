package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "협찬 가능 제품 리스트(상세 포함) 응답 DTO")
public class SponsorProductListResponseDto {

    @Schema(description = "제품 대표 이미지 URL")
    private String thumbnailImageUrl;

    @Schema(description = "브랜드 ID")
    private Long brandId;

    @Schema(description = "브랜드명")
    private String brandName;

    @Schema(description = "제품 ID")
    private Long productId;

    @Schema(description = "제품명")
    private String productName;

    @Schema(description = "제품 이미지 URL 목록")
    private List<String> productImageUrls;

    @Schema(description = "카테고리 목록")
    private List<String> categories;

    @Schema(description = "협찬 정보")
    private SponsorInfoDto sponsorInfo;

    @Schema(description = "액션 정보")
    private ActionDto action;

    public static SponsorProductListResponseDto from(
            Brand brand,
            BrandAvailableSponsor product,
            List<String> productImageUrls,
            List<String> categories,
            SponsorInfoDto sponsorInfo,
            ActionDto action
    ) {
        String thumbnail = null;
        if (productImageUrls != null && !productImageUrls.isEmpty()) {
            thumbnail = productImageUrls.get(0);
        }

        return SponsorProductListResponseDto.builder()
                .thumbnailImageUrl(thumbnail)
                .brandId(brand.getId())
                .brandName(brand.getBrandName())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrls(productImageUrls)
                .categories(categories)
                .sponsorInfo(sponsorInfo)
                .action(action)
                .build();
    }
}
