package com.example.RealMatch.user.presentation.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

public record MyCampaignListResponseDto(
        List<CampaignItem> campaigns,
        int currentPage,
        int totalPages,
        long totalElements
) {
    @Builder
    public record CampaignItem(
            String applicationId,
            String brandName,
            String title,
            String status,              // REVIEWING, MATCHED, REJECTED
            LocalDate appliedDate,
            Integer matchingRate
    ) {}

    public static MyCampaignListResponseDto of(
            List<CampaignItem> campaigns,
            int currentPage,
            int totalPages,
            long totalElements
    ) {
        return new MyCampaignListResponseDto(campaigns, currentPage, totalPages, totalElements);
    }
}
