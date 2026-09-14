package com.example.RealMatch.notification.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryClaimService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDeliveryClaimService.class);

    private static final List<DeliveryStatus> CLAIMABLE_STATUSES =
            List.of(DeliveryStatus.PENDING, DeliveryStatus.RETRY);

    private final NotificationDeliveryRepository deliveryRepository;

    @Transactional
    public boolean claimDelivery(UUID deliveryId) {
        int updated = deliveryRepository.claimDelivery(
                deliveryId,
                DeliveryStatus.IN_PROGRESS,
                LocalDateTime.now(),
                CLAIMABLE_STATUSES);

        if (updated == 0) {
            LOG.debug("[DeliveryClaim] Claim failed (already processed). deliveryId={}", deliveryId);
        }
        return updated > 0;
    }

    @Transactional
    public void markSent(UUID deliveryId, String providerMessageId) {
        NotificationDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            LOG.warn("[DeliveryClaim] Delivery not found for markSent. deliveryId={}", deliveryId);
            return;
        }
        delivery.markAsSent(providerMessageId);
        LOG.debug("[DeliveryClaim] Marked SENT. deliveryId={}, providerId={}", deliveryId, providerMessageId);
    }

    @Transactional
    public void recordFailure(UUID deliveryId, String reason) {
        NotificationDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            LOG.warn("[DeliveryClaim] Delivery not found for recordFailure. deliveryId={}", deliveryId);
            return;
        }
        delivery.recordFailure(reason);
        LOG.warn("[DeliveryClaim] Recorded failure. deliveryId={}, attempt={}, newStatus={}",
                deliveryId, delivery.getAttemptCount(), delivery.getStatus());
    }

    @Transactional
    public void markPermanentlyFailed(UUID deliveryId, String reason) {
        NotificationDelivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            LOG.warn("[DeliveryClaim] Delivery not found for permanent failure. deliveryId={}", deliveryId);
            return;
        }
        delivery.markAsPermanentlyFailed(reason);
        LOG.warn("[DeliveryClaim] Marked PERMANENTLY FAILED. deliveryId={}, reason={}", deliveryId, reason);
    }

    @Transactional
    public void skipDelivery(UUID deliveryId) {
        deliveryRepository.findById(deliveryId).ifPresent(NotificationDelivery::skip);
    }
}
