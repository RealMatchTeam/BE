package com.example.RealMatch.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.application.service.NotificationDispatchService;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.OutboxStatus;
import com.example.RealMatch.notification.infrastructure.messaging.NotificationDeliveryConsumer;
import com.example.RealMatch.notification.infrastructure.messaging.NotificationDeliveryMessage;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.rabbitmq.client.Channel;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class DeliveryReliabilityTest {
    @Test
    void staleWorkerCannotOverwriteNewAttempt() {
        var delivery = NotificationDelivery.builder().notificationId(UUID.randomUUID()).channel(NotificationChannel.PUSH)
                .status(DeliveryStatus.PENDING).build();
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(delivery, "id", id);
        var repository = mock(NotificationDeliveryRepository.class);
        when(repository.findForUpdate(id)).thenReturn(Optional.of(delivery));
        var service = new NotificationDeliveryClaimService(repository);
        var old = service.claim(id);
        delivery.recordFailure("lease expired");
        ReflectionTestUtils.setField(delivery, "nextRetryAt", LocalDateTime.now().minusSeconds(1));
        var current = service.claim(id);
        service.complete(old, DeliveryStatus.SENT, "stale");
        assertEquals(DeliveryStatus.IN_PROGRESS, delivery.getStatus());
        service.complete(current, DeliveryStatus.SENT, "accepted");
        service.complete(old, DeliveryStatus.RETRY, "stale failure");
        assertEquals(DeliveryStatus.SENT, delivery.getStatus());
        assertEquals("accepted", delivery.getProviderMessageId());
    }

    @Test
    void crashesConsumeAttemptBudgetAndStopAfterFive() {
        var delivery = NotificationDelivery.builder().status(DeliveryStatus.PENDING).build();
        for (int i = 0; i < 5; i++) {
            assertTrue(delivery.claim(LocalDateTime.now().plusDays(30)));
            delivery.recordFailure("worker crashed");
        }
        assertEquals(5, delivery.getAttemptCount());
        assertEquals(DeliveryStatus.FAILED, delivery.getStatus());
        assertFalse(delivery.claim(LocalDateTime.now().plusYears(1)));
    }

    @Test
    void outboxCompletionIsFencedAndRetriesWait() {
        var outbox = NotificationOutbox.builder().build();
        var now = LocalDateTime.now();
        var old = outbox.claim(now);
        outbox.complete(old, "connection failed", now);
        assertNull(outbox.claim(now));
        var current = outbox.claim(now.plusMinutes(1));
        outbox.complete(old, null, now);
        assertEquals(OutboxStatus.SENDING, outbox.getStatus());
        outbox.complete(current, null, now);
        assertEquals(OutboxStatus.SENT, outbox.getStatus());
    }

    @Test
    void ackFailureNeverReclassifiesBusinessSuccessAndPayloadCannotChooseRecipient() throws Exception {
        var dispatch = mock(NotificationDispatchService.class);
        var consumer = new NotificationDeliveryConsumer(dispatch, new SimpleMeterRegistry());
        var channel = mock(Channel.class);
        UUID id = UUID.randomUUID();
        var message = new NotificationDeliveryMessage(id.toString(), "forged-recipient", "INVALID_CHANNEL");
        doThrow(new IOException("connection lost after processing")).when(channel).basicAck(7L, false);
        assertThrows(IOException.class, () -> consumer.handleDelivery(message, channel, 7L));
        verify(dispatch).dispatch(id);
        verifyNoMoreInteractions(dispatch);
        verify(channel, never()).basicNack(7L, false, false);
    }

    @Test
    void invalidMessageGoesToDeadLetterAndDatabaseFailureIsRecoverable() throws Exception {
        var dispatch = mock(NotificationDispatchService.class);
        var consumer = new NotificationDeliveryConsumer(dispatch, new SimpleMeterRegistry());
        var channel = mock(Channel.class);
        consumer.handleDelivery(new NotificationDeliveryMessage("bad", null, null), channel, 1L);
        verifyNoInteractions(dispatch);
        verify(channel).basicNack(1L, false, false);
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("DB down")).when(dispatch).dispatch(id);
        consumer.handleDelivery(new NotificationDeliveryMessage(id.toString(), null, null), channel, 2L);
        verify(channel).basicNack(2L, false, false);
    }
}
