package com.example.RealMatch.notification.application.service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationCategory;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.notification.exception.NotificationErrorCode;
import com.example.RealMatch.notification.infrastructure.redis.NotificationUnreadCountCache;
import com.example.RealMatch.notification.presentation.dto.response.NotificationDateGroup;
import com.example.RealMatch.notification.presentation.dto.response.NotificationListResponse;
import com.example.RealMatch.notification.presentation.dto.response.NotificationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCountCache unreadCountCache;

    private static final DateTimeFormatter DATE_LABEL_FORMATTER =
            DateTimeFormatter.ofPattern("yy.MM.dd (E)", Locale.KOREAN);

    public NotificationListResponse getNotifications(Long userId, String filter, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<NotificationKind> kinds = resolveKinds(filter);

        Page<Notification> notificationPage;
        if (kinds == null) {
            notificationPage = notificationRepository.findByUserId(userId, pageRequest);
        } else {
            notificationPage = notificationRepository.findByUserIdAndKindIn(userId, kinds, pageRequest);
        }

        List<NotificationResponse> items = notificationPage.getContent().stream()
                .map(NotificationResponse::from)
                .toList();

        List<NotificationDateGroup> groups = buildDateGroups(notificationPage.getContent());

        long unreadCount = getUnreadCount(userId);

        return new NotificationListResponse(
                items,
                groups,
                unreadCount,
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.getNumber(),
                notificationPage.getSize()
        );
    }

    public long getUnreadCount(Long userId) {
        return unreadCountCache.get(userId)
                .orElseGet(() -> {
                    long count = notificationRepository.countUnreadByUserId(userId);
                    unreadCountCache.set(userId, count);
                    return count;
                });
    }

    private List<NotificationKind> resolveKinds(String filter) {
        if (filter == null || "ALL".equalsIgnoreCase(filter)) {
            return null;
        }

        try {
            NotificationCategory category = NotificationCategory.valueOf(filter.toUpperCase());
            return category.getKinds();
        } catch (IllegalArgumentException e) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_INVALID_FILTER);
        }
    }

    private List<NotificationDateGroup> buildDateGroups(List<Notification> notifications) {
        return notifications.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> NotificationDateGroup.of(
                        entry.getKey(),
                        entry.getValue().intValue(),
                        DATE_LABEL_FORMATTER))
                .toList();
    }
}
