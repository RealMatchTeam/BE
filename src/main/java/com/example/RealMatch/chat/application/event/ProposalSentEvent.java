package com.example.RealMatch.chat.application.event;

import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;

public record ProposalSentEvent(
        Long roomId,
        ChatProposalCardPayloadResponse payload
) {
}
