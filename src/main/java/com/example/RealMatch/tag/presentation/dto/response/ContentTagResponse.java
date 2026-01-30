package com.example.RealMatch.tag.presentation.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentTagResponse {
    private List<TagItemResponse> formats;      // FORMAT
    private List<TagItemResponse> categories;   // CATEGORY
    private List<TagItemResponse> tones;        // TONE
    private List<TagItemResponse> involvements; // INVOLVEMENT
    private List<TagItemResponse> usageRanges;  // USAGE_RANGE

    public record TagItemResponse(
            UUID id,
            String name        // kor_name
    ) {}
}
