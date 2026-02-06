package com.example.RealMatch.chat.domain.enums;

public enum ChatSystemMessageKind {
    PROPOSAL_CARD(true),
    RE_PROPOSAL_CARD(true),
    PROPOSAL_STATUS_NOTICE(true),
    MATCHED_CAMPAIGN_CARD(true),
    APPLY_CARD(true),
    APPLY_STATUS_NOTICE(true);

    private final boolean payloadRequired;

    ChatSystemMessageKind(boolean payloadRequired) {
        this.payloadRequired = payloadRequired;
    }

    public boolean isPayloadRequired() {
        return payloadRequired;
    }
}
