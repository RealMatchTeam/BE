package com.example.RealMatch.chat.application.event;

import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;

/**
 * 채팅 모듈 내부에서 사용되는 제안 전송 이벤트
 */
public record ProposalSentEvent(
        Long roomId,
        ChatProposalCardPayloadResponse payload,
        boolean isReProposal
) {
}
