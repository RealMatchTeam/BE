package com.example.RealMatch.notification.presentation.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.notification.application.service.NotificationQueryService;
import com.example.RealMatch.notification.application.service.NotificationService;
import com.example.RealMatch.notification.presentation.dto.response.NotificationListResponse;
import com.example.RealMatch.notification.presentation.dto.response.ReadAllResponse;
import com.example.RealMatch.notification.presentation.dto.response.UnreadCountResponse;
import com.example.RealMatch.notification.presentation.swagger.NotificationSwagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationSwagger {

    private final NotificationService notificationService;
    private final NotificationQueryService notificationQueryService;

    @Override
    @GetMapping
    public CustomResponse<NotificationListResponse> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationListResponse response = notificationQueryService.getNotifications(
                userDetails.getUserId(), filter, page, size);
        return CustomResponse.ok(response);
    }

    @Override
    @PatchMapping("/{id}/read")
    public CustomResponse<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        notificationService.markAsRead(userDetails.getUserId(), id);
        return CustomResponse.ok(null);
    }

    @Override
    @PatchMapping("/read-all")
    public CustomResponse<ReadAllResponse> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int updatedCount = notificationService.markAllAsRead(userDetails.getUserId());
        return CustomResponse.ok(new ReadAllResponse(updatedCount));
    }

    @Override
    @GetMapping("/unread-count")
    public CustomResponse<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        long count = notificationQueryService.getUnreadCount(userDetails.getUserId());
        return CustomResponse.ok(new UnreadCountResponse(count));
    }
}
