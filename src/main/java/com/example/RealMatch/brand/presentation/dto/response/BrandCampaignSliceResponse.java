package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

public record BrandCampaignSliceResponse(
        List<BrandCampaignResponseDto> campaigns,
        boolean hasNext
) {
}

