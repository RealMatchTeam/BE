package com.example.RealMatch.chat.application.event.proposal;

import java.util.UUID;

import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;

/**
 * 채팅 모듈 내부에서 사용되는 제안 전송 이벤트.
 * 비즈니스 이벤트(CampaignProposalSentEvent)를 변환한 결과물입니다.
 */
public record ProposalSentEvent(
        String eventId,      // 이벤트 중복 처리용 고유 ID (UUID 기반)
        Long roomId,
        ChatProposalCardPayloadResponse payload,
        boolean isReProposal
) {
    public static String generateEventId(Long proposalId, boolean isReProposal) {
        if (proposalId == null) {
            throw new IllegalArgumentException("proposalId cannot be null");
        }
        String type = isReProposal ? "RE_PROPOSAL_SENT" : "PROPOSAL_SENT";
        return String.format("%s:%d:%s", type, proposalId, UUID.randomUUID().toString());
    }
}
