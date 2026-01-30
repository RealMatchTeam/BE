package com.example.RealMatch.business.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.entity.CampaignProposalContentTag;
import com.example.RealMatch.tag.domain.enums.ContentTagType;
import com.example.RealMatch.tag.presentation.dto.response.ContentTagResponse;

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

    private ContentTagResponse contentTags;

    public static CampaignProposalDetailResponse from(CampaignProposal proposal) {
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
                .contentTags(toContentTagResponse(proposal.getTags()))
                .build();
    }

    private static ContentTagResponse toContentTagResponse(
            List<CampaignProposalContentTag> tags
    ) {
        Map<ContentTagType, List<ContentTagResponse.TagItemResponse>> map =
                new EnumMap<>(ContentTagType.class);

        for (CampaignProposalContentTag tag : tags) {
            ContentTagType type = tag.getTagContent().getTagType();

            map.computeIfAbsent(type, k -> new ArrayList<>())
                    .add(toTagItem(tag));
        }

        return new ContentTagResponse(
                map.getOrDefault(ContentTagType.FORMAT, List.of()),
                map.getOrDefault(ContentTagType.CATEGORY, List.of()),
                map.getOrDefault(ContentTagType.TONE, List.of()),
                map.getOrDefault(ContentTagType.INVOLVEMENT, List.of()),
                map.getOrDefault(ContentTagType.USAGE_RANGE, List.of())
        );
    }

    private static ContentTagResponse.TagItemResponse toTagItem(
            CampaignProposalContentTag tag
    ) {
        String baseName = tag.getTagContent().getKorName();
        String name = tag.getCustomTagValue() != null && !tag.getCustomTagValue().isBlank()
                ? baseName + " (" + tag.getCustomTagValue() + ")"
                : baseName;

        return new ContentTagResponse.TagItemResponse(
                tag.getTagContent().getId(),
                name
        );
    }
}
