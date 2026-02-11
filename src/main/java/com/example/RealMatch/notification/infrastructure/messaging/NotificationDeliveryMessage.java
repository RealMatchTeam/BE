package com.example.RealMatch.notification.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RabbitMQ를 통해 전달되는 알림 배달 메시지
 *
 * <p>OutboxPublisher가 발행하고 DeliveryConsumer가 소비한다.
 * JSON 직렬화/역직렬화를 위해 불변 객체로 설계한다.
 */
public class NotificationDeliveryMessage {

    private final String deliveryId;
    private final String notificationId;
    private final String channel;

    @JsonCreator
    public NotificationDeliveryMessage(
            @JsonProperty("deliveryId") String deliveryId,
            @JsonProperty("notificationId") String notificationId,
            @JsonProperty("channel") String channel) {
        this.deliveryId = deliveryId;
        this.notificationId = notificationId;
        this.channel = channel;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "NotificationDeliveryMessage{"
                + "deliveryId='" + deliveryId + '\''
                + ", notificationId='" + notificationId + '\''
                + ", channel='" + channel + '\''
                + '}';
    }
}
