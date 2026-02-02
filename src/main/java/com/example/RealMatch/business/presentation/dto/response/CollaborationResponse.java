package com.example.RealMatch.business.presentation.dto.response;

import java.time.LocalDate;

import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

public record CollaborationResponse(
        Long campaignId,           // 캠페인 기반일 때만 채움
        Long proposalId,           // 제안 기반일 때만 채움

        String brandName,
        String thumbnailUrl,
        String title,
        ProposalStatus status,

        LocalDate startDate,
        LocalDate endDate,

        CollaborationType type  // APPLIED / SENT / RECEIVED
        ) {


        /**
         * 내가 지원한 캠페인
         */
        public static CollaborationResponse fromApply(CampaignApply apply) {
                return new CollaborationResponse(
                        apply.getCampaign().getId(),
                        null,

                        apply.getCampaign().getBrand().getBrandName(),
                        apply.getCampaign().getImageUrl(),
                        apply.getCampaign().getTitle(),
                        apply.getProposalStatus(),

                        apply.getCampaign().getStartDate(),
                        apply.getCampaign().getEndDate(),
                        CollaborationType.APPLIED
                );
        }

        /**
         * 내가 받은 제안 (브랜드 → 나)
         */
        public static CollaborationResponse fromReceivedProposal(CampaignProposal proposal) {
                return new CollaborationResponse(
                        proposal.getCampaign() != null
                                ? proposal.getCampaign().getId()
                                : null,
                        proposal.getId(),

                        proposal.getBrand().getBrandName(),
                        proposal.getBrand().getLogoUrl(),
                        proposal.getTitle(),
                        proposal.getStatus(),

                        proposal.getStartDate(),
                        proposal.getEndDate(),

                        CollaborationType.RECEIVED
                );
        }

        /**
         * 내가 보낸 제안
         */
        public static CollaborationResponse fromSentProposal(CampaignProposal proposal) {
                return new CollaborationResponse(
                        proposal.getCampaign() != null
                                ? proposal.getCampaign().getId()
                                : null,
                        proposal.getId(),

                        proposal.getBrand().getBrandName(),
                        proposal.getBrand().getLogoUrl(),
                        proposal.getTitle(),
                        proposal.getStatus(),

                        proposal.getStartDate(),
                        proposal.getEndDate(),
                        CollaborationType.SENT
                );
        }
}
