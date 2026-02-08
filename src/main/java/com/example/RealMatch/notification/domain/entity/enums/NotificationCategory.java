package com.example.RealMatch.notification.domain.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum NotificationCategory {

    PROPOSAL,
    MATCHING,
    SETTLEMENT,
    CHAT;

    public List<NotificationKind> getKinds() {
        return Arrays.stream(NotificationKind.values())
                .filter(kind -> kind.getCategory() == this)
                .toList();
    }
}
