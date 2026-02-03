package com.example.RealMatch.business.presentation.dto.response;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.RealMatch.business.domain.entity.CampaignProposalContentTag;
import com.example.RealMatch.tag.domain.enums.ContentTagType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignContentTagResponse {
    private List<TagItemResponse> formats;         // FORMAT
    private List<TagItemResponse> categories;      // CATEGORY
    private List<TagItemResponse> tones;           // TONE
    private List<TagItemResponse> involvements;    // INVOLVEMENT
    private List<TagItemResponse> usageRanges;     // USAGE_RANGE

    public record TagItemResponse(
            Long id,
            String name
    ) {}

    public static CampaignContentTagResponse from(List<CampaignProposalContentTag> tags) {
        Map<ContentTagType, List<TagItemResponse>> map = new EnumMap<>(ContentTagType.class);

        for (CampaignProposalContentTag tag : tags) {
            ContentTagType type = tag.getTagContent().getTagType();

            map.computeIfAbsent(type, k -> new ArrayList<>())
                    .add(toTagItem(tag));
        }

        return new CampaignContentTagResponse(
                map.getOrDefault(ContentTagType.FORMAT, List.of()),
                map.getOrDefault(ContentTagType.CATEGORY, List.of()),
                map.getOrDefault(ContentTagType.TONE, List.of()),
                map.getOrDefault(ContentTagType.INVOLVEMENT, List.of()),
                map.getOrDefault(ContentTagType.USAGE_RANGE, List.of())
        );
    }

    private static TagItemResponse toTagItem(CampaignProposalContentTag tag) {
        String baseName = tag.getTagContent().getKorName();
        String name = tag.getCustomTagValue() != null && !tag.getCustomTagValue().isBlank()
                ? baseName + " (" + tag.getCustomTagValue() + ")"
                : baseName;

        return new TagItemResponse(tag.getTagContent().getId(), name);
    }
}
