package com.example.RealMatch.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.notification.application.service.FcmTokenService;
import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;
import com.example.RealMatch.notification.domain.repository.FcmTokenRepository;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.presentation.controller.FcmTokenController;
import com.example.RealMatch.notification.presentation.dto.request.FcmTokenRemoveRequest;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/isolated-notification-tests.yml",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@ContextConfiguration(classes = NotificationRepositoryTest.JpaConfig.class)
class NotificationRepositoryTest {

    @Configuration
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = FcmTokenRepository.class)
    @EntityScan(basePackageClasses = FcmToken.class)
    static class JpaConfig {
    }

    @Autowired
    private FcmTokenRepository tokens;

    @Autowired
    private NotificationDeliveryRepository deliveries;

    @Autowired
    private NotificationOutboxRepository outboxes;

    @Autowired
    private EntityManager entityManager;

    @Test
    void newlyClaimedOldOutboxIsNotMistakenForStuckPublish() {
        NotificationOutbox outbox = outboxes.saveAndFlush(NotificationOutbox.builder()
                .notificationId(UUID.randomUUID()).deliveryId(UUID.randomUUID())
                .channel(NotificationChannel.PUSH).build());
        entityManager.createQuery("update NotificationOutbox o set o.updatedAt = :old where o.id = :id")
                .setParameter("old", LocalDateTime.now().minusDays(2)).setParameter("id", outbox.getId())
                .executeUpdate();
        entityManager.clear();

        assertEquals(1, outboxes.claimOutbox(outbox.getId(), OutboxStatus.SENDING, OutboxStatus.PENDING));
        assertEquals(0, outboxes.recoverStuckOutbox(OutboxStatus.SENDING, OutboxStatus.PENDING,
                LocalDateTime.now().minusMinutes(5)));
        assertTrue(outboxes.findById(outbox.getId()).orElseThrow().getUpdatedAt()
                .isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void tokenDeletionUsesAuthenticatedOwnerAndIsIdempotent() {
        tokens.saveAndFlush(FcmToken.builder().userId(1L).token("private-token").build());
        FcmTokenController controller = new FcmTokenController(new FcmTokenService(tokens));
        FcmTokenRemoveRequest request = new FcmTokenRemoveRequest("private-token");

        controller.removeToken(new CustomUserDetails(2L, "other", "CREATOR", "other@example.com"), request);
        assertTrue(tokens.findByToken("private-token").isPresent());

        CustomUserDetails owner = new CustomUserDetails(1L, "owner", "CREATOR", "owner@example.com");
        controller.removeToken(owner, request);
        controller.removeToken(owner, request);
        assertFalse(tokens.findByToken("private-token").isPresent());
    }

    @Test
    void deliveryCannotBeClaimedBeforeBackoffExpires() {
        NotificationDelivery delivery = delivery();
        delivery.recordFailure("temporarily unavailable");
        deliveries.saveAndFlush(delivery);
        LocalDateTime dueAt = delivery.getNextRetryAt();

        assertEquals(0, claim(delivery.getId(), dueAt.minusSeconds(1)));
        assertEquals(1, claim(delivery.getId(), dueAt.plusSeconds(1)));
        assertEquals(0, claim(delivery.getId(), dueAt.plusSeconds(2)));
    }

    @Test
    void skippedDeliveryIsPersistedAndCannotBeRetried() {
        NotificationDelivery delivery = delivery();
        delivery.recordFailure("previous failure");
        delivery.skip();
        deliveries.saveAndFlush(delivery);

        assertEquals(0, claim(delivery.getId(), LocalDateTime.now().plusDays(1)));
        assertEquals(DeliveryStatus.SKIPPED, deliveries.findById(delivery.getId()).orElseThrow().getStatus());
    }

    private int claim(UUID id, LocalDateTime now) {
        return deliveries.claimDelivery(id, DeliveryStatus.IN_PROGRESS, now,
                List.of(DeliveryStatus.PENDING, DeliveryStatus.RETRY));
    }

    private NotificationDelivery delivery() {
        return NotificationDelivery.builder().notificationId(UUID.randomUUID())
                .channel(NotificationChannel.PUSH).status(DeliveryStatus.PENDING)
                .idempotencyKey(UUID.randomUUID().toString()).build();
    }
}
