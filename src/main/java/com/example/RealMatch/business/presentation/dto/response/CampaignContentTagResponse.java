package com.example.RealMatch.business.presentation.dto.response;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.example.RealMatch.business.domain.entity.CampaignProposalContentTag;
import com.example.RealMatch.business.domain.enums.ProposalTagType;

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
            String name
    ) {}

    public static CampaignContentTagResponse from(List<CampaignProposalContentTag> tags) {
        Map<ProposalTagType, List<TagItemResponse>> map = new EnumMap<>(ProposalTagType.class);

        for (CampaignProposalContentTag tag : tags) {
            ProposalTagType type = tag.getTagType();

            map.computeIfAbsent(type, k -> new ArrayList<>())
                    .add(toTagItem(tag));
        }

        return new CampaignContentTagResponse(
                map.getOrDefault(ProposalTagType.FORMAT, List.of()),
                map.getOrDefault(ProposalTagType.CATEGORY, List.of()),
                map.getOrDefault(ProposalTagType.TONE, List.of()),
                map.getOrDefault(ProposalTagType.INVOLVEMENT, List.of()),
                map.getOrDefault(ProposalTagType.USAGE_RANGE, List.of())
        );
    }

    private static TagItemResponse toTagItem(CampaignProposalContentTag tag) {
        String baseName = tag.getTagName();
        String name = tag.getCustomTagValue() != null && !tag.getCustomTagValue().isBlank()
                ? baseName + " (" + tag.getCustomTagValue() + ")"
                : baseName;

        return new TagItemResponse(name);
    }
}
