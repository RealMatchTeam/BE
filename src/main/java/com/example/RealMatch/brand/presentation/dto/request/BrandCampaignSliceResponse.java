package com.example.RealMatch.brand.presentation.dto.request;

import java.util.List;

import com.example.RealMatch.brand.presentation.dto.response.BrandCampaignResponseDto;

public record BrandCampaignSliceResponse(
        List<BrandCampaignResponseDto> campaigns,
        boolean hasNext
) {
}

