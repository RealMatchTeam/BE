package com.example.RealMatch.brand.presentation.dto.response;

import java.time.LocalDate;

import com.example.RealMatch.campaign.domain.enums.CampaignRecrutingStatus;


public record BrandCampaignResponseDto(
        Long campaignId,
        String title,
        LocalDate recruitStartDate,
        LocalDate recruitEndDate,
        CampaignRecrutingStatus status
) {
}
