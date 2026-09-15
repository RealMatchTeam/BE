package com.example.RealMatch.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.infrastructure.persistence.JpaFcmTokenRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationDeliveryRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationOutboxRepository;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.rabbitmq.client.ConnectionFactory;

import jakarta.persistence.EntityManager;

@EnabledIfEnvironmentVariable(named = "REALMATCH_INFRA_TEST", matches = "true")
@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/isolated-infrastructure-tests.yml",
        "spring.datasource.url=${REALMATCH_TEST_MYSQL_URL:jdbc:mysql://localhost:13306/realmatch_messaging_test}",
        "spring.datasource.username=root",
        "spring.datasource.password=${REALMATCH_TEST_MYSQL_PASSWORD:local-test-only}",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = MessagingInfrastructureTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MessagingInfrastructureTest {
    @Configuration
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = JpaFcmTokenRepository.class)
    @EntityScan(basePackageClasses = {FcmToken.class, ChatMessage.class})
    @Import(NotificationDeliveryClaimService.class)
    static class Config {
    }

    @Autowired private JpaFcmTokenRepository tokens;
    @Autowired private JpaNotificationDeliveryRepository deliveries;
    @Autowired private JpaNotificationOutboxRepository outboxes;
    @Autowired private NotificationDeliveryClaimService claims;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private EntityManager entityManager;

    @Test
    void generatedSchemaPreservesCaseSensitiveIdentities() {
        var tx = new TransactionTemplate(transactions);
        String key = UUID.randomUUID().toString();
        tx.executeWithoutResult(status -> {
            tokens.upsert(11L, key + "A", "upper", LocalDateTime.now());
            tokens.upsert(12L, key + "a", "lower", LocalDateTime.now());
            tokens.upsert(13L, key + "a ", "trailing space", LocalDateTime.now());
            entityManager.persist(ChatMessage.createUserMessage(1L, 1L, ChatMessageType.TEXT, "upper", null, "Client-A"));
            entityManager.persist(ChatMessage.createUserMessage(1L, 1L, ChatMessageType.TEXT, "lower", null, "client-a"));
            entityManager.flush();
        });
        assertEquals(11L, tokens.findByToken(key + "A").orElseThrow().getUserId());
        assertEquals(12L, tokens.findByToken(key + "a").orElseThrow().getUserId());
        assertEquals(13L, tokens.findByToken(key + "a ").orElseThrow().getUserId());
        Number binaryColumns = (Number) entityManager.createNativeQuery("""
                select count(*) from information_schema.columns where table_schema=database()
                and collation_name='utf8mb4_0900_bin' and (
                    (table_name='fcm_token' and column_name='token')
                    or (table_name in ('notification','notification_delivery') and column_name='idempotency_key')
                    or (table_name='chat_message' and column_name in ('client_message_id','system_event_id')))
                """).getSingleResult();
        assertEquals(5, binaryColumns.intValue());
    }

    @Test
    void mysqlClaimsOnlyOneConcurrentWorkerAndNativeTokenUpsertConverges() throws Exception {
        var delivery = deliveries.saveAndFlush(NotificationDelivery.builder().notificationId(UUID.randomUUID())
                .channel(NotificationChannel.PUSH).status(DeliveryStatus.PENDING).build());
        var start = new CyclicBarrier(2);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> {
                start.await(5, TimeUnit.SECONDS); return claims.claim(delivery.getId());
            });
            var second = workers.submit(() -> {
                start.await(5, TimeUnit.SECONDS); return claims.claim(delivery.getId());
            });
            int winners = (first.get(10, TimeUnit.SECONDS) == null ? 0 : 1)
                    + (second.get(10, TimeUnit.SECONDS) == null ? 0 : 1);
            assertEquals(1, winners);
        }
        var tx = new TransactionTemplate(transactions);
        String token = UUID.randomUUID().toString();
        tx.executeWithoutResult(status -> tokens.upsert(1L, token, "first", LocalDateTime.now()));
        tx.executeWithoutResult(status -> tokens.upsert(2L, token, "second", LocalDateTime.now()));
        assertEquals(2L, tokens.findByToken(token).orElseThrow().getUserId());
        assertEquals("second", tokens.findByToken(token).orElseThrow().getDeviceInfo());
        var outbox = outboxes.saveAndFlush(NotificationOutbox.builder().deliveryId(delivery.getId())
                .notificationId(delivery.getNotificationId()).channel(NotificationChannel.PUSH).build());
        tx.executeWithoutResult(status -> outboxes.findForUpdate(outbox.getId()).orElseThrow().claim(LocalDateTime.now()));
        assertTrue(outboxes.findStuck(LocalDateTime.now().minusMinutes(5), PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void brokerConfirmsPublishAndRedeliversUnacknowledgedMessage() throws Exception {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(Integer.parseInt(System.getenv().getOrDefault("REALMATCH_TEST_RABBIT_PORT", "13672")));
        factory.setUsername(System.getenv().getOrDefault("REALMATCH_TEST_RABBIT_USER", "local-test"));
        factory.setPassword(System.getenv().getOrDefault("REALMATCH_TEST_RABBIT_PASSWORD", "local-test-only"));
        factory.setConnectionTimeout(5000);
        try (var connection = factory.newConnection(); var channel = connection.createChannel()) {
            String queue = "reliability-" + UUID.randomUUID();
            channel.queueDeclare(queue, false, false, true, null);
            channel.confirmSelect();
            channel.basicPublish("", queue, null, new byte[] {1});
            assertTrue(channel.waitForConfirms(5000));
            var message = channel.basicGet(queue, false);
            assertFalse(message.getEnvelope().isRedeliver());
            channel.basicNack(message.getEnvelope().getDeliveryTag(), false, true);
            var replay = channel.basicGet(queue, false);
            assertTrue(replay.getEnvelope().isRedeliver());
            channel.basicAck(replay.getEnvelope().getDeliveryTag(), false);
            channel.queueDelete(queue);
        }
    }
}
