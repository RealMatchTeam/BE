package com.example.RealMatch.chat.presentation.dto.response;

public record ChatProposalActionButtonsResponse(
        ChatProposalActionButtonResponse accept,
        ChatProposalActionButtonResponse reject
) {
}
