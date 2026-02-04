package com.example.RealMatch.chat.application.event.proposal;

import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

/**
 * 채팅 모듈 내부용 제안 상태 변경 이벤트.
 * 비즈니스 이벤트(CampaignProposalStatusChangedEvent)를 변환한 결과물입니다.
 */
public record ProposalStatusChangedEvent(
        String eventId,      // 이벤트 중복 처리용 고유 ID
        Long proposalId,
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        ChatProposalStatus newStatus,
        Long actorUserId     // 상태 변경을 수행한 사용자 ID (수락/거절한 사용자)
) {
    public static String generateEventId(Long proposalId, ChatProposalStatus newStatus) {
        return String.format("PROPOSAL_STATUS:%d:%s:%d", proposalId, newStatus, System.currentTimeMillis());
    }
}
