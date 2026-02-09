package com.example.RealMatch.notification.infrastructure.sender;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.repository.FcmTokenRepository;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;

/**
 * FCM(Firebase Cloud Messaging) 기반 웹 푸시 발송기.
 * 사용자의 모든 디바이스 토큰에 푸시를 발송한다.
 */
@Component
public class FcmNotificationSender implements NotificationChannelSender {

    private static final Logger LOG = LoggerFactory.getLogger(FcmNotificationSender.class);

    @Nullable
    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;

    public FcmNotificationSender(
            @Autowired(required = false) @Nullable FirebaseMessaging firebaseMessaging,
            FcmTokenRepository fcmTokenRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.fcmTokenRepository = fcmTokenRepository;
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

        List<FcmToken> tokens = fcmTokenRepository.findByUserId(notification.getUserId());
        if (tokens.isEmpty()) {
            throw new PermanentSendFailureException(
                    "No FCM tokens found for userId=" + notification.getUserId());
        }

        StringBuilder messageIds = new StringBuilder();
        int successCount = 0;
        int failCount = 0;

        for (FcmToken fcmToken : tokens) {
            try {
                String messageId = sendToToken(notification, fcmToken.getToken());
                if (messageId != null) {
                    if (!messageIds.isEmpty()) {
                        messageIds.append(",");
                    }
                    messageIds.append(messageId);
                    successCount++;
                }
            } catch (FirebaseMessagingException e) {
                handleFcmError(fcmToken, e);
                failCount++;
            }
        }

        if (successCount == 0 && failCount > 0) {
            throw new PermanentSendFailureException(
                    "All FCM sends failed. tokens=" + tokens.size());
        }

        LOG.info("[FCM] Push sent. userId={}, success={}, fail={}, notificationId={}",
                notification.getUserId(), successCount, failCount, notification.getId());
        return messageIds.toString();
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

    /**
     * FCM 에러 처리. UNREGISTERED/INVALID_ARGUMENT 토큰은 자동 삭제한다.
     */
    private void handleFcmError(FcmToken fcmToken, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        if (errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            LOG.warn("[FCM] Invalid token removed. userId={}, token={}..., error={}",
                    fcmToken.getUserId(),
                    fcmToken.getToken().substring(0, Math.min(10, fcmToken.getToken().length())),
                    errorCode);
            fcmTokenRepository.delete(fcmToken);
        } else {
            LOG.error("[FCM] Send failed. userId={}, error={}, message={}",
                    fcmToken.getUserId(), errorCode, e.getMessage());
        }
    }
}
