package com.example.RealMatch.notification.infrastructure.sender;

import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

/**
 * 채널별 알림 발송 전략 인터페이스.
 * 각 구현체(FCM, Email)는 이 인터페이스를 구현하고,
 * NotificationDeliveryConsumer에서 채널에 맞는 구현체를 선택하여 발송한다.
 */
public interface NotificationChannelSender {

    NotificationChannel getChannel();

    String send(Notification notification) throws Exception;

    boolean isAvailable();
}
