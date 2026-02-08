package com.example.RealMatch.notification.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.RealMatch.notification.domain.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationResponse(
        UUID id,
        String kind,
        String category,
        String title,
        String body,
        Long campaignId,
        Long proposalId,
        @JsonProperty("isRead") Boolean isRead,
        LocalDateTime createdAt,
        String iconType
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getKind().name(),
                notification.getKind().getCategory().name(),
                notification.getTitle(),
                notification.getBody(),
                notification.getCampaignId(),
                notification.getProposalId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getKind().name()
        );
    }
}
