package com.example.RealMatch.chat.application.event;

import com.example.RealMatch.business.domain.enums.ProposalStatus;

public record ProposalStatusChangedEvent(
        Long proposalId,
        Long campaignId,
        Long brandUserId,
        Long creatorUserId,
        ProposalStatus newStatus
) {
}
