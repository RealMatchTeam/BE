package com.example.RealMatch.notification.application.service;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.application.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.application.repository.NotificationRepository;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.exception.NotificationErrorCode;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationChannelResolver channelResolver;
    private final UserRepository userRepository;

    public Notification create(CreateNotificationCommand command) {
        if (command == null || command.getEventId() == null || command.getEventId().isBlank()
                || command.getEventId().length() > 100 || command.getUserId() == null || command.getKind() == null) {
            throw new IllegalArgumentException("A valid eventId, userId and kind are required");
        }
        // ponytail: serialize inbox creation per recipient; use an atomic upsert if contention matters.
        long lockStarted = System.nanoTime();
        userRepository.findByIdForUpdate(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Notification recipient does not exist"));
        if (System.nanoTime() - lockStarted > 500_000_000L) {
            LOG.warn("Notification recipient lock exceeded 500 ms. userId={}", command.getUserId());
        }
        String key = command.getEventId() + ":" + command.getKind() + ":" + command.getUserId();
        Notification existing = notificationRepository.findByIdempotencyKeyIncludingDeleted(key).orElse(null);
        if (existing != null) {
            return existing;
        }
        Notification notification = Notification.builder()
                .idempotencyKey(key)
                .userId(command.getUserId())
                .kind(command.getKind())
                .title(command.getTitle())
                .body(command.getBody())
                .referenceType(command.getReferenceType())
                .referenceId(command.getReferenceId())
                .campaignId(command.getCampaignId())
                .proposalId(command.getProposalId())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        createPendingDeliveriesWithOutbox(
                savedNotification.getId(),
                command.getKind(),
                command.getEventId(),
                command.getUserId());
        return savedNotification;
    }

    private void createPendingDeliveriesWithOutbox(UUID notificationId, NotificationKind kind,
                                                    String eventId, Long receiverId) {
        Set<NotificationChannel> channels = channelResolver.resolveChannels(kind, receiverId);

        for (NotificationChannel channel : channels) {
            String idempotencyKey = generateIdempotencyKey(eventId, kind, receiverId, channel);
            NotificationDelivery delivery = NotificationDelivery.builder()
                    .notificationId(notificationId)
                    .channel(channel)
                    .status(DeliveryStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .build();
            NotificationDelivery savedDelivery = notificationDeliveryRepository.save(delivery);

            NotificationOutbox outbox = NotificationOutbox.builder()
                    .deliveryId(savedDelivery.getId())
                    .notificationId(notificationId)
                    .channel(channel)
                    .build();
            notificationOutboxRepository.save(outbox);

            LOG.debug("[Notification] Created delivery + outbox. idempotencyKey={}, channel={}",
                    idempotencyKey, channel);
        }
    }

    private String generateIdempotencyKey(String eventId, NotificationKind kind,
                                           Long receiverId, NotificationChannel channel) {
        return String.format("%s:%s:%d:%s", eventId, kind, receiverId, channel);
    }

    public void markAsRead(Long userId, UUID notificationId) {
        Notification notification = findNotificationForUser(userId, notificationId);
        notification.markAsRead();
    }

    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    public void softDelete(Long userId, UUID notificationId) {
        Notification notification = findNotificationForUser(userId, notificationId);
        notification.softDelete();
    }

    private Notification findNotificationForUser(Long userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        return notification;
    }
}
