package com.example.RealMatch.business.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.entity.CampaignProposalTag;
import com.example.RealMatch.tag.presentation.dto.response.TagResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignProposalDetailResponse {

    private UUID proposalId;

    private Long brandId;
    private Long creatorId;

    private String title;
    private String description;

    private Long rewardAmount;
    private Long productId;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
    private String refusalReason;

    private LocalDateTime createdAt;

    private TagResponse tags;

    public static CampaignProposalDetailResponse from(
            CampaignProposal proposal,
            List<CampaignProposalTag> tags
    ) {
        return CampaignProposalDetailResponse.builder()
                .proposalId(proposal.getId())
                .brandId(proposal.getBrand().getId())
                .creatorId(proposal.getCreator().getId())
                .title(proposal.getTitle())
                .description(proposal.getCampaignDescription())
                .rewardAmount(Long.valueOf(proposal.getRewardAmount()))
                .productId(proposal.getProductId())
                .startDate(proposal.getStartDate())
                .endDate(proposal.getEndDate())
                .status(proposal.getStatus().name())
                .refusalReason(proposal.getRefusalReason())
                .createdAt(proposal.getCreatedAt())
                .tags(toTagResponse(tags))
                .build();
    }

    private static TagResponse toTagResponse(List<CampaignProposalTag> proposalTags) {
        Map<String, List<TagResponse.TagItem>> categories = new HashMap<>();

        for (CampaignProposalTag proposalTag : proposalTags) {
            String category = proposalTag.getTag().getTagCategory();
            String tagName = resolveTagName(proposalTag);

            categories.computeIfAbsent(category, k -> new ArrayList<>())
                    .add(new TagResponse.TagItem(
                            proposalTag.getTag().getId(),
                            tagName
                    ));
        }

        String tagType = proposalTags.isEmpty() ? null : proposalTags.get(0).getTag().getTagType();
        return new TagResponse(tagType, categories);
    }

    private static String resolveTagName(CampaignProposalTag proposalTag) {
        String baseName = proposalTag.getTag().getTagName();
        if (proposalTag.getCustomTagValue() != null && !proposalTag.getCustomTagValue().isBlank()) {
            return baseName + " (" + proposalTag.getCustomTagValue() + ")";
        }
        return baseName;
    }
}
