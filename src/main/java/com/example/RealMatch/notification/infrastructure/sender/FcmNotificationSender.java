package com.example.RealMatch.notification.infrastructure.sender;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.application.exception.PermanentSendFailureException;
import com.example.RealMatch.notification.application.port.NotificationChannelSender;
import com.example.RealMatch.notification.application.repository.FcmTokenRepository;
import com.example.RealMatch.notification.application.repository.PushReceiptRepository;
import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.PushReceipt;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;

@Component
public class FcmNotificationSender implements NotificationChannelSender {

    private static final Logger LOG = LoggerFactory.getLogger(FcmNotificationSender.class);

    @Nullable
    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final PushReceiptRepository receipts;

    public FcmNotificationSender(
            @Autowired(required = false) @Nullable FirebaseMessaging firebaseMessaging,
            FcmTokenRepository fcmTokenRepository, PushReceiptRepository receipts) {
        this.firebaseMessaging = firebaseMessaging;
        this.fcmTokenRepository = fcmTokenRepository;
        this.receipts = receipts;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean isAvailable() {
        return firebaseMessaging != null;
    }

    @Override
    public String send(Notification notification) throws Exception {
        if (firebaseMessaging == null) {
            throw new PermanentSendFailureException("Firebase is not initialized. FCM push disabled.");
        }

        List<FcmToken> tokens = fcmTokenRepository.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(notification.getUserId(), LocalDateTime.now().minusDays(90));
        if (tokens.isEmpty()) {
            throw new PermanentSendFailureException(
                    "No FCM tokens found for userId=" + notification.getUserId());
        }

        String providerMessageId = null;
        int successCount = 0;
        FirebaseMessagingException retryableFailure = null;
        FirebaseMessagingException permanentFailure = null;

        for (FcmToken fcmToken : tokens) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("FCM send cancelled");
            }
            if (receipts.existsByNotificationIdAndTokenId(notification.getId(), fcmToken.getId())) {
                successCount++;
                continue;
            }
            try {
                String messageId = sendToToken(notification, fcmToken.getToken());
                receipts.saveAndFlush(new PushReceipt(notification.getId(), fcmToken.getId()));
                providerMessageId = messageId;
                successCount++;
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    fcmTokenRepository.delete(fcmToken);
                }
                if (isRetryable(e.getMessagingErrorCode())) {
                    retryableFailure = e;
                } else {
                    permanentFailure = e;
                }
                LOG.warn("[FCM] Send failed. userId={}, notificationId={}, error={}",
                        notification.getUserId(), notification.getId(), e.getMessagingErrorCode());
            }
        }

        if (retryableFailure != null) {
            throw retryableFailure;
        }
        if (successCount == 0) {
            throw new PermanentSendFailureException(
                    "All FCM sends failed permanently. tokens=" + tokens.size(), permanentFailure);
        }

        LOG.info("[FCM] Push sent. userId={}, success={}, fail={}, notificationId={}",
                notification.getUserId(), successCount, tokens.size() - successCount, notification.getId());
        return providerMessageId;
    }

    private String sendToToken(Notification notification, String token) throws FirebaseMessagingException {
        com.google.firebase.messaging.Notification fcmNotification =
                com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build();

        Message message = Message.builder()
                .setNotification(fcmNotification)
                .setToken(token)
                .putData("notificationId", notification.getId().toString())
                .putData("kind", notification.getKind().name())
                .putData("referenceType",
                        notification.getReferenceType() != null ? notification.getReferenceType().name() : "")
                .putData("referenceId",
                        notification.getReferenceId() != null ? notification.getReferenceId() : "")
                .build();

        return firebaseMessaging.send(message);
    }

    private boolean isRetryable(MessagingErrorCode errorCode) {
        if (errorCode == null) {
            return true;
        }
        return switch (errorCode) {
            case UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED -> true;
            case UNREGISTERED, INVALID_ARGUMENT, SENDER_ID_MISMATCH, THIRD_PARTY_AUTH_ERROR -> false;
        };
    }
}
