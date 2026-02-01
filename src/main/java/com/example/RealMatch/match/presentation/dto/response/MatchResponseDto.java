package com.example.RealMatch.match.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private String userType;
    private List<String> typeTag;
    private HighMatchingBrandListDto highMatchingBrandList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighMatchingBrandListDto {
        private Integer count;
        private List<BrandDto> brands;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandDto {
        private Long brandId;
        private String brandName;
        private String logoUrl;
        private Integer matchingRatio;
    }
}
