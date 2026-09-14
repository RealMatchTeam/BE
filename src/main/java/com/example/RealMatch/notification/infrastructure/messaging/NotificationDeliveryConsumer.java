package com.example.RealMatch.notification.infrastructure.messaging;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.application.service.NotificationDispatchService;
import com.example.RealMatch.notification.application.service.NotificationOutboxService;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationDeliveryConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDeliveryConsumer.class);

    private final NotificationDeliveryClaimService claimService;
    private final NotificationOutboxService outboxService;
    private final NotificationDispatchService dispatchService;

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleDelivery(NotificationDeliveryMessage message,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        LOG.debug("[DeliveryConsumer] Received message. {}", message);

        try {
            processMessage(message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (isTransient(e)) {
                handleTransientError(message, channel, deliveryTag, e);
            } else {
                LOG.error("[DeliveryConsumer] Permanent error. Sending to DLQ. message={}, error={}",
                        message, e.getMessage(), e);
                safeNack(channel, deliveryTag);
            }
        }
    }

    private void handleTransientError(NotificationDeliveryMessage message, Channel channel,
                                      long deliveryTag, Exception e) {
        UUID deliveryId = null;
        UUID notificationId = null;
        NotificationChannel channelEnum = null;
        try {
            deliveryId = UUID.fromString(message.getDeliveryId());
            notificationId = UUID.fromString(message.getNotificationId());
            channelEnum = NotificationChannel.valueOf(message.getChannel());
        } catch (Exception parseEx) {
            LOG.warn("[DeliveryConsumer] Poison message (cannot parse). Sending to DLQ. message={}",
                    message, parseEx);
            safeNack(channel, deliveryTag);
            return;
        }

        try {
            claimService.recordFailure(deliveryId, e.getMessage());
            outboxService.createRetryOutboxIfAbsent(deliveryId, notificationId, channelEnum);
            channel.basicAck(deliveryTag, false);
            LOG.warn("[DeliveryConsumer] Transient error. Scheduled retry. deliveryId={}, error={}",
                    deliveryId, e.getMessage());
        } catch (Exception retryEx) {
            LOG.error("[DeliveryConsumer] Transient but failed to schedule retry. ACK to avoid DLQ pollution. "
                    + "deliveryId={}, originalError={}, scheduleError={}",
                    deliveryId, e.getMessage(), retryEx.getMessage(), retryEx);
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException ackEx) {
                LOG.error("[DeliveryConsumer] Failed to ACK after schedule retry failure. deliveryTag={}",
                        deliveryTag, ackEx);
            }
        }
    }

    private static boolean isTransient(Throwable e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof DataAccessException) {
                return true;
            }
            if (t instanceof SocketTimeoutException || t instanceof ConnectException || t instanceof UnknownHostException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private void processMessage(NotificationDeliveryMessage message) {
        UUID deliveryId = UUID.fromString(message.getDeliveryId());
        UUID notificationId = UUID.fromString(message.getNotificationId());
        NotificationChannel notificationChannel = NotificationChannel.valueOf(message.getChannel());

        dispatchService.dispatch(deliveryId, notificationId, notificationChannel);
    }

    private void safeNack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            LOG.error("[DeliveryConsumer] Failed to NACK. deliveryTag={}", deliveryTag, e);
        }
    }
}
