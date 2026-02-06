package com.example.RealMatch.business.application.event;

import com.example.RealMatch.business.domain.enums.ProposalStatus;

/**
 * 캠페인 지원의 상태가 변경되었을 때 발행되는 이벤트
 */
public record CampaignApplyStatusChangedEvent(
        Long applyId,
        Long campaignId,
        Long creatorUserId,  // 지원한 크리에이터 ID
        Long brandUserId,    // 캠페인을 소유한 브랜드의 사용자 ID
        ProposalStatus newStatus,
        Long actorUserId     // 상태 변경을 수행한 사용자 ID (취소/수락/거절한 사용자)
) {
}
