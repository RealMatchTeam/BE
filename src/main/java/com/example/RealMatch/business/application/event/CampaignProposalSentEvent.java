package com.example.RealMatch.business.application.event;

import com.example.RealMatch.business.domain.enums.ProposalDirection;
import com.example.RealMatch.business.domain.enums.ProposalStatus;

/**
 * 캠페인 제안이 생성되거나 재제안 되었을 때 발행되는 이벤트
 */
public record CampaignProposalSentEvent(
        Long proposalId,
        Long brandUserId,
        Long creatorUserId,
        Long campaignId,
        String campaignName,
        String campaignSummary,
        ProposalStatus proposalStatus,
        ProposalDirection proposalDirection,
        boolean isReProposal
) {
}
