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

/**
 * Delivery 상태 전이 서비스. Consumer에서 호출.
 * 각 메서드가 독립 TX → 외부 API(FCM/SMTP)가 TX 밖에서 실행됨을 보장.
 * 흐름: claimDelivery() → (외부 발송) → markSent() / recordFailure()
 */
@Service
@RequiredArgsConstructor
public class NotificationDeliveryClaimService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDeliveryClaimService.class);

    private static final List<DeliveryStatus> CLAIMABLE_STATUSES =
            List.of(DeliveryStatus.PENDING, DeliveryStatus.RETRY);

    private final NotificationDeliveryRepository deliveryRepository;

    /** PENDING/RETRY → IN_PROGRESS 조건절 UPDATE. 1 row면 성공. */
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

    /** IN_PROGRESS → SENT. */
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

    /** 일시적 실패. attemptCount 기준 RETRY(backoff) 또는 FAILED. */
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

    /** 영구 실패(잘못된 토큰, 미존재 이메일 등). 재시도 불가. */
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
}
