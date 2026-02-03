package com.example.RealMatch.campaign.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignTagResponse {
    private List<TagItemResponse> formats;         // FORMAT
    private List<TagItemResponse> categories;      // CATEGORY
    private List<TagItemResponse> tones;           // TONE
    private List<TagItemResponse> involvements;    // INVOLVEMENT
    private List<TagItemResponse> usageRanges;     // USAGE_RANGE

    public record TagItemResponse(
            Long id,
            String name
    ) {}
}
