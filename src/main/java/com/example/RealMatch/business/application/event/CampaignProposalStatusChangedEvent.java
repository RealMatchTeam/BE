package com.example.RealMatch.business.application.event;

import com.example.RealMatch.business.domain.enums.ProposalStatus;

/**
 * 캠페인 제안의 상태가 변경되었을 때 발행되는 이벤트
 */
public record CampaignProposalStatusChangedEvent(
        Long proposalId,
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        ProposalStatus newStatus,
        Long actorUserId  // 상태 변경을 수행한 사용자 ID (수락/거절한 사용자)
) {
}
