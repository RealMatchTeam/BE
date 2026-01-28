package com.example.RealMatch.user.presentation.dto.response;

import java.util.Collections;
import java.util.List;

public record MyScrapResponseDto(
        String type,
        int totalCount,
        List<BrandScrap> brandList,
        List<CampaignScrap> campaignList
) {
    public record BrandScrap(
            Long brandId,
            String brandName,
            String brandLogo,
            int matchingRate,
            List<String> hashtags,
            boolean isScraped
    ) {}

    public record CampaignScrap(
            Long campaignId,
            String brandName,
            String campaignTitle,
            String brandLogo,
            int matchingRate,
            int reward,
            int dDay,
            int currentApplicants,
            int totalRecruits,
            boolean isScraped
    ) {}

    // 브랜드 타입 응답 생성
    public static MyScrapResponseDto ofBrandType(List<BrandScrap> brandList) {
        return new MyScrapResponseDto(
                "brand",
                brandList.size(),
                brandList,
                Collections.emptyList()
        );
    }

    // 캠페인 타입 응답 생성
    public static MyScrapResponseDto ofCampaignType(List<CampaignScrap> campaignList) {
        return new MyScrapResponseDto(
                "campaign",
                campaignList.size(),
                Collections.emptyList(),
                campaignList
        );
    }
}
