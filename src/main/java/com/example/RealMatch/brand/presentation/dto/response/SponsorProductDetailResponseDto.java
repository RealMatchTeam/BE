package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorProductDetailResponseDto {
    private Long brandId;
    private String brandName;
    private Long productId;
    private String productName;
    private String productDescription;
    private List<String> productImageUrls;
    private List<String> categories;
    private SponsorInfoDto sponsorInfo;
    private ActionDto action;
}
