package com.example.RealMatch.chat.application.event.proposal;

import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;

/**
 * 채팅 모듈 내부에서 사용되는 제안 전송 이벤트.
 * 비즈니스 이벤트(CampaignProposalSentEvent)를 변환한 결과물입니다.
 */
public record ProposalSentEvent(
        String eventId,      // 이벤트 중복 처리용 고유 ID
        Long roomId,
        ChatProposalCardPayloadResponse payload,
        boolean isReProposal
) {
    public static String generateEventId(Long proposalId, boolean isReProposal) {
        String type = isReProposal ? "RE_PROPOSAL" : "PROPOSAL";
        return String.format("%s:%d:%d", type, proposalId, System.currentTimeMillis());
    }
}
