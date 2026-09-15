package com.example.RealMatch.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.notification.application.service.NotificationOutboxService;
import com.example.RealMatch.notification.domain.entity.NotificationOutbox;
import com.example.RealMatch.notification.infrastructure.messaging.NotificationOutboxPublisher;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

class NotificationOutboxPublisherTest {

    @ParameterizedTest
    @ValueSource(strings = {"ack", "nack", "returned", "timeout", "connection-error"})
    void onlyConfirmedAndRoutedPublishIsMarkedSent(String outcome) {
        NotificationOutboxService store = mock(NotificationOutboxService.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        NotificationOutboxPublisher publisher = new NotificationOutboxPublisher(store, rabbit);
        ReflectionTestUtils.setField(publisher, "confirmTimeoutMs", 1L);
        NotificationOutbox outbox = NotificationOutbox.builder().deliveryId(UUID.randomUUID())
                .notificationId(UUID.randomUUID()).channel(NotificationChannel.PUSH).build();
        UUID outboxId = UUID.randomUUID();
        ReflectionTestUtils.setField(outbox, "id", outboxId);
        when(store.findPendingOutbox(50)).thenReturn(List.of(outbox));
        UUID token = UUID.randomUUID();
        when(store.claimOutbox(outboxId)).thenReturn(token);

        doAnswer(call -> {
            verify(store, never()).complete(any(), any(), ArgumentMatchers.isNull());
            CorrelationData correlation = call.getArgument(3);
            if ("connection-error".equals(outcome)) {
                throw new IllegalStateException("Connection closed");
            }
            if ("returned".equals(outcome)) {
                correlation.setReturned(new ReturnedMessage(new Message(new byte[0]),
                        312, "NO_ROUTE", "notification.exchange", "notification.delivery"));
            }
            if (!"timeout".equals(outcome)) {
                correlation.getFuture().complete(new CorrelationData.Confirm(!"nack".equals(outcome), outcome));
            }
            return null;
        }).when(rabbit).convertAndSend(any(String.class), any(String.class), any(Object.class), any(CorrelationData.class));

        publisher.publishPendingOutbox();

        if ("ack".equals(outcome)) {
            verify(store).complete(outboxId, token, null);
            verify(store, never()).complete(any(), any(), ArgumentMatchers.isNotNull());
        } else {
            verify(store, never()).complete(any(), any(), ArgumentMatchers.isNull());
            verify(store).complete(eq(outboxId), eq(token), ArgumentMatchers.isNotNull());
        }
    }
}
