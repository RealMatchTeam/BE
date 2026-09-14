package com.example.RealMatch.notification.application.port;

import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;

public interface NotificationChannelSender {

    NotificationChannel getChannel();

    String send(Notification notification) throws Exception;

    boolean isAvailable();
}
