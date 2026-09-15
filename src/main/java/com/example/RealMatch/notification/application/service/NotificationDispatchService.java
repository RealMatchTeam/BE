package com.example.RealMatch.notification.application.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;

import com.example.RealMatch.notification.application.exception.PermanentSendFailureException;
import com.example.RealMatch.notification.application.port.NotificationChannelSender;
import com.example.RealMatch.notification.application.repository.NotificationRepository;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.UserRepository;

import jakarta.annotation.PreDestroy;

@lombok.extern.slf4j.Slf4j
@Service
public class NotificationDispatchService {
    private final ExecutorService sends = new ThreadPoolExecutor(
            2, 2, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), task -> {
                Thread thread = new Thread(task, "notification-send");
                thread.setDaemon(true);
                return thread;
            });

    @PreDestroy
    public void close() {
        sends.shutdownNow();
    }

    private final NotificationDeliveryClaimService claims;
    private final NotificationRepository notifications;
    private final NotificationChannelResolver channels;
    private final UserRepository users;
    private final Map<NotificationChannel, NotificationChannelSender> senders = new EnumMap<>(NotificationChannel.class);

    public NotificationDispatchService(NotificationDeliveryClaimService claims, NotificationRepository notifications,
                                       NotificationChannelResolver channels, UserRepository users,
                                       List<NotificationChannelSender> senders) {
        this.claims = claims;
        this.notifications = notifications;
        this.channels = channels;
        this.users = users;
        for (var sender : senders) {
            if (this.senders.putIfAbsent(sender.getChannel(), sender) != null) {
                throw new IllegalArgumentException("Duplicate notification sender");
            }
        }
    }

    public void dispatch(UUID id) {
        var claim = claims.claim(id);
        if (claim == null) {
            return;
        }
        DeliveryStatus outcome;
        String detail = null;
        try {
            var notification = notifications.findById(claim.notificationId()).orElse(null);
            if (notification == null || users.findById(notification.getUserId()).isEmpty()
                    || !channels.isEnabled(notification.getUserId(), claim.channel())) {
                outcome = DeliveryStatus.SKIPPED;
            } else {
                var sender = senders.get(claim.channel());
                if (sender == null || !sender.isAvailable()) {
                    throw new IllegalStateException("Notification sender unavailable");
                }
                detail = sendWithDeadline(sender, notification);
                outcome = DeliveryStatus.SENT;
            }
        } catch (PermanentSendFailureException ex) {
            outcome = DeliveryStatus.FAILED;
            detail = ex.getMessage();
        } catch (Exception ex) {
            log.warn("Notification delivery failed. deliveryId={}, attempt={}", id, claim.attempt(), ex);
            outcome = DeliveryStatus.RETRY;
            detail = ex.getMessage();
        }
        // A failure of this transaction is recovered by the lease; never rewrite a successful send as a new attempt.
        claims.complete(claim, outcome, detail);
    }
    private String sendWithDeadline(NotificationChannelSender sender, Notification notification) throws Exception {
        var future = sends.submit(() -> sender.send(notification));
        try {
            return future.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw ex;
        } catch (InterruptedException ex) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw ex;
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw ex;
        }
    }
}
