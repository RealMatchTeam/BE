package com.example.RealMatch.chat.application.event;

import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

/**
 * 채팅 모듈 내부용 제안 상태 변경 이벤트
 */
public record ProposalStatusChangedEvent(
        Long proposalId,
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        ChatProposalStatus newStatus
) {
}
