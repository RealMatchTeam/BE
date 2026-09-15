package com.example.RealMatch.notification.application.dto.response;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> items,
        List<NotificationDateGroup> groups,
        long unreadCount,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
}
