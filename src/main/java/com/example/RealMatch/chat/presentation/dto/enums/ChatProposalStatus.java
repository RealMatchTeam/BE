package com.example.RealMatch.chat.presentation.dto.enums;

public enum ChatProposalStatus {
    MATCHED("매칭"),
    REVIEWING("검토중"),
    REJECTED("거절"),
    NONE("");

    private final String label;

    ChatProposalStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public String labelOrNull() {
        return label == null || label.isBlank() ? null : label;
    }
}
