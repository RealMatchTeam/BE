package com.example.RealMatch.chat.presentation.dto.enums;

import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;

public enum ChatRoomFilterStatus {
    MATCHED,
    REVIEWING,
    REJECTED,
    ALL;

    public ChatProposalStatus toProposalStatus() {
        return switch (this) {
            case MATCHED -> ChatProposalStatus.MATCHED;
            case REVIEWING -> ChatProposalStatus.REVIEWING;
            case REJECTED -> ChatProposalStatus.REJECTED;
            case ALL -> throw new IllegalArgumentException("ALL cannot be converted to ChatProposalStatus");
        };
    }
}
