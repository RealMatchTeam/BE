package com.example.RealMatch.notification.application.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.RealMatch.notification.application.exception.PermanentSendFailureException;
import com.example.RealMatch.notification.application.port.NotificationChannelSender;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

@Service
public class NotificationDispatchService {

    private final NotificationDeliveryClaimService claimService;
    private final NotificationRepository notificationRepository;
    private final NotificationChannelResolver channelResolver;
    private final Map<NotificationChannel, NotificationChannelSender> senderMap;

    public NotificationDispatchService(NotificationDeliveryClaimService claimService,
                                       NotificationRepository notificationRepository,
                                       NotificationChannelResolver channelResolver,
                                       List<NotificationChannelSender> senders) {
        this.claimService = claimService;
        this.notificationRepository = notificationRepository;
        this.channelResolver = channelResolver;
        this.senderMap = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelSender sender : senders) {
            if (senderMap.putIfAbsent(sender.getChannel(), sender) != null) {
                throw new IllegalArgumentException("Duplicate sender for channel: " + sender.getChannel());
            }
        }
    }

    public void dispatch(UUID deliveryId, UUID notificationId, NotificationChannel channel) {
        if (!claimService.claimDelivery(deliveryId)) {
            return;
        }

        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            claimService.markPermanentlyFailed(deliveryId, "Notification not found: " + notificationId);
            return;
        }
        if (!channelResolver.isEnabled(notification.getUserId(), channel)) {
            claimService.skipDelivery(deliveryId);
            return;
        }

        NotificationChannelSender sender = senderMap.get(channel);
        if (sender == null || !sender.isAvailable()) {
            claimService.recordFailure(deliveryId, "No available sender for channel: " + channel);
            return;
        }

        final String providerMessageId;
        try {
            providerMessageId = sender.send(notification);
        } catch (PermanentSendFailureException e) {
            claimService.markPermanentlyFailed(deliveryId, e.getMessage());
            return;
        } catch (Exception e) {
            claimService.recordFailure(deliveryId, e.getMessage());
            return;
        }
        claimService.markSent(deliveryId, providerMessageId);
    }
}
