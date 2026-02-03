package com.example.RealMatch.business.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.RealMatch.business.domain.entity.CampaignProposal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignProposalDetailResponse {

    private Long proposalId;

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

    private CampaignContentTagResponse contentTags;

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
                .contentTags(CampaignContentTagResponse.from(proposal.getTags()))
                .build();
    }
}
