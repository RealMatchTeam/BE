package com.example.RealMatch.notification.infrastructure.messaging;

import java.io.IOException;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.application.service.NotificationDispatchService;
import com.rabbitmq.client.Channel;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeliveryConsumer {
    private final NotificationDispatchService dispatch;
    private final MeterRegistry metrics;

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void handleDelivery(NotificationDeliveryMessage message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        UUID id;
        try {
            id = UUID.fromString(message.getDeliveryId());
        } catch (RuntimeException ex) {
            metrics.counter("notification.consume", "result", "invalid").increment();
            channel.basicNack(tag, false, false);
            return;
        }
        try {
            // Legacy notificationId/channel fields are ignored; the claimed DB row is authoritative.
            metrics.timer("notification.dispatch.duration").record(() -> dispatch.dispatch(id));
        } catch (RuntimeException ex) {
            log.error("Delivery failed; persistent lease/outbox recovery will retry. deliveryId={}", id, ex);
            metrics.counter("notification.consume", "result", "failed").increment();
            channel.basicNack(tag, false, false);
            return;
        }
        metrics.counter("notification.consume", "result", "processed").increment();
        // ACK errors must never change a completed Delivery.
        channel.basicAck(tag, false);
    }
}
