package com.example.RealMatch.notification.domain.entity.enums;

public enum NotificationKind {

    // 제안 관련 (PROPOSAL 카테고리)
    PROPOSAL_RECEIVED(NotificationCategory.PROPOSAL),
    PROPOSAL_SENT(NotificationCategory.PROPOSAL),
    CAMPAIGN_APPLIED(NotificationCategory.PROPOSAL),

    // 매칭 관련 (MATCHING 카테고리)
    CAMPAIGN_MATCHED(NotificationCategory.MATCHING),
    AUTO_CONFIRMED(NotificationCategory.MATCHING),

    // 정산 관련 (SETTLEMENT 카테고리)
    CAMPAIGN_COMPLETED(NotificationCategory.SETTLEMENT),
    SETTLEMENT_READY(NotificationCategory.SETTLEMENT),

    // 채팅 (CHAT 카테고리)
    CHAT_MESSAGE(NotificationCategory.CHAT);

    private final NotificationCategory category;

    NotificationKind(NotificationCategory category) {
        this.category = category;
    }

    public NotificationCategory getCategory() {
        return category;
    }
}
