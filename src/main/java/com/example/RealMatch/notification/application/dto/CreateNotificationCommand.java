package com.example.RealMatch.notification.application.dto;

import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.entity.enums.ReferenceType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateNotificationCommand {

    private final String eventId;  // 이벤트 식별자 (멱등성 보장용)
    private final Long userId;
    private final NotificationKind kind;
    private final String title;
    private final String body;
    private final ReferenceType referenceType;
    private final String referenceId;
    private final Long campaignId;
    private final Long proposalId;
}
