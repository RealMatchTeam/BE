package com.example.RealMatch.chat.application.event;

import java.util.UUID;

import com.example.RealMatch.business.domain.enums.ProposalStatus;

public record ProposalStatusChangedEvent(
        UUID proposalId,
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        ProposalStatus newStatus
) {
}
