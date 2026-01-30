package com.example.RealMatch.user.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record MyCampaignDetailResponseDto(
        String applicationId,
        String brandName,
        String brandLogoUrl,
        Integer matchingRate,
        String status,
        String type,                    // "APPLY" 또는 "PROPOSAL"
        String campaignName,
        String campaignDescription,
        BenefitSection benefit,
        ScheduleSection schedule,
        List<String> additionalSections
) {
    @Builder
    public record BenefitSection(
            String productName,
            Long rewardAmount
    ) {}

    @Builder
    public record ScheduleSection(
            LocalDate startDate,
            LocalDate endDate
    ) {}
}
