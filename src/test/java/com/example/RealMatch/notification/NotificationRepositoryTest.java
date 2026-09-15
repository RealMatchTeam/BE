package com.example.RealMatch.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.notification.application.dto.request.FcmTokenRemoveRequest;
import com.example.RealMatch.notification.application.service.FcmTokenService;
import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.infrastructure.persistence.JpaFcmTokenRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationDeliveryRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationOutboxRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationRepository;
import com.example.RealMatch.notification.presentation.controller.FcmTokenController;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.UserRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/isolated-notification-tests.yml",
        "spring.datasource.url=jdbc:h2:mem:notification_queries;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@ContextConfiguration(classes = NotificationRepositoryTest.JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Configuration
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = JpaFcmTokenRepository.class)
    @EntityScan(basePackageClasses = FcmToken.class)
    static class JpaConfig {
    }

    @Autowired
    private JpaFcmTokenRepository tokens;

    @Autowired
    private JpaNotificationDeliveryRepository deliveries;

    @Autowired
    private JpaNotificationOutboxRepository outboxes;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JpaNotificationRepository notifications;

    @Test
    void outboxPollingOrdersByDueTimeAndExcludesFutureRetries() {
        var now = LocalDateTime.now();
        var later = outboxes.saveAndFlush(NotificationOutbox.builder().notificationId(UUID.randomUUID())
                .deliveryId(UUID.randomUUID()).channel(NotificationChannel.PUSH).build());
        var earlier = outboxes.saveAndFlush(NotificationOutbox.builder().notificationId(UUID.randomUUID())
                .deliveryId(UUID.randomUUID()).channel(NotificationChannel.PUSH).build());
        org.springframework.test.util.ReflectionTestUtils.setField(later, "nextAttemptAt", now.plusHours(1));
        org.springframework.test.util.ReflectionTestUtils.setField(earlier, "nextAttemptAt", now.minusHours(1));
        outboxes.flush();
        assertEquals(java.util.List.of(earlier.getId()), outboxes.findDue(now, PageRequest.of(0, 10))
                .stream().map(NotificationOutbox::getId).toList());
        assertEquals(java.util.List.of(earlier.getId(), later.getId()), outboxes.findDue(now.plusHours(2), PageRequest.of(0, 10))
                .stream().map(NotificationOutbox::getId).toList());
    }

    @Test
    void deletedInboxIsHiddenButRetainsItsIdempotencyRecord() {
        String key = UUID.randomUUID().toString();
        Notification notification = notifications.saveAndFlush(Notification.builder().userId(1L)
                .kind(NotificationKind.CHAT_MESSAGE).title("Title").body("Body").idempotencyKey(key).build());
        notification.softDelete();
        notifications.flush();
        entityManager.clear();

        assertTrue(notifications.findById(notification.getId()).isEmpty());
        assertTrue(notifications.findByUserId(1L, PageRequest.of(0, 10)).isEmpty());
        assertEquals(notification.getId(), notifications.findByIdempotencyKeyIncludingDeleted(key).orElseThrow().getId());
    }

    @Test
    void pendingOutboxesAreExcludedBeforeApplyingRecoveryPageLimit() {
        NotificationDelivery blocked = deliveries.saveAndFlush(delivery());
        NotificationDelivery eligible = deliveries.saveAndFlush(delivery());
        outboxes.saveAndFlush(NotificationOutbox.builder().deliveryId(blocked.getId())
                .notificationId(blocked.getNotificationId()).channel(blocked.getChannel()).build());

        var now = LocalDateTime.now().plusHours(1);
        var candidates = deliveries.findDispatchable(now, now.minusMinutes(30), PageRequest.of(0, 1));

        assertEquals(1, candidates.size());
        assertEquals(eligible.getId(), candidates.getFirst().getId());
    }

    @Test
    void newlyClaimedOldOutboxIsNotMistakenForStuckPublish() {
        NotificationOutbox outbox = outboxes.saveAndFlush(NotificationOutbox.builder()
                .notificationId(UUID.randomUUID()).deliveryId(UUID.randomUUID())
                .channel(NotificationChannel.PUSH).build());
        entityManager.createQuery("update NotificationOutbox o set o.updatedAt = :old where o.id = :id")
                .setParameter("old", LocalDateTime.now().minusDays(2)).setParameter("id", outbox.getId())
                .executeUpdate();
        entityManager.clear();

        outboxes.findForUpdate(outbox.getId()).orElseThrow().claim(LocalDateTime.now());
        outboxes.flush();
        assertTrue(outboxes.findStuck(LocalDateTime.now().minusMinutes(5), PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void tokenDeletionUsesAuthenticatedOwnerAndIsIdempotent() {
        tokens.saveAndFlush(FcmToken.builder().userId(1L).token("private-token").build());
        FcmTokenController controller = new FcmTokenController(new FcmTokenService(tokens, Mockito.mock(UserRepository.class)));
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
        return deliveries.findForUpdate(id).orElseThrow().claim(now) ? 1 : 0;
    }

    private NotificationDelivery delivery() {
        return NotificationDelivery.builder().notificationId(UUID.randomUUID())
                .channel(NotificationChannel.PUSH).status(DeliveryStatus.PENDING)
                .idempotencyKey(UUID.randomUUID().toString()).build();
    }
}
