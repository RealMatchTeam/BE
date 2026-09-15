package com.example.RealMatch.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.notification.application.exception.PermanentSendFailureException;
import com.example.RealMatch.notification.application.repository.FcmTokenRepository;
import com.example.RealMatch.notification.application.repository.PushReceiptRepository;
import com.example.RealMatch.notification.domain.entity.FcmToken;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.infrastructure.sender.FcmNotificationSender;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;

class FcmNotificationSenderTest {

    private final FirebaseMessaging firebase = mock(FirebaseMessaging.class);
    private final FcmTokenRepository tokens = mock(FcmTokenRepository.class);
    private final PushReceiptRepository receipts = mock(PushReceiptRepository.class);
    private final FcmNotificationSender sender = new FcmNotificationSender(firebase, tokens, receipts);
    private final FcmToken token = FcmToken.builder().userId(1L).token("device-1").build();
    private final Notification notification = Notification.builder().userId(1L)
            .kind(NotificationKind.CHAT_MESSAGE).title("Message").body("Hello").build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(token, "id", 1L);
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        when(tokens.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(ArgumentMatchers.eq(1L), any())).thenReturn(List.of(token));
    }

    @ParameterizedTest
    @EnumSource(value = MessagingErrorCode.class, names = {"UNAVAILABLE", "INTERNAL", "QUOTA_EXCEEDED"})
    void transientErrorsRemainRetryableWithoutDeletingToken(MessagingErrorCode code) throws Exception {
        FirebaseMessagingException failure = failure(code);
        when(firebase.send(any(Message.class))).thenThrow(failure);

        assertSame(failure, assertThrows(FirebaseMessagingException.class, () -> sender.send(notification)));
        verify(tokens, never()).delete(any());
    }

    @ParameterizedTest
    @EnumSource(value = MessagingErrorCode.class,
            names = {"INVALID_ARGUMENT", "SENDER_ID_MISMATCH", "THIRD_PARTY_AUTH_ERROR"})
    void permanentErrorsDoNotDeletePossiblyValidTokens(MessagingErrorCode code) throws Exception {
        FirebaseMessagingException failure = failure(code);
        when(firebase.send(any(Message.class))).thenThrow(failure);

        assertThrows(PermanentSendFailureException.class, () -> sender.send(notification));
        verify(tokens, never()).delete(any());
    }

    @Test
    void unregisteredTokenIsDeleted() throws Exception {
        FirebaseMessagingException failure = failure(MessagingErrorCode.UNREGISTERED);
        when(firebase.send(any(Message.class))).thenThrow(failure);

        assertThrows(PermanentSendFailureException.class, () -> sender.send(notification));
        verify(tokens).delete(token);
    }

    @Test
    void partialTransientFailureDoesNotDisappearBehindAnotherDevicesSuccess() throws Exception {
        when(tokens.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(ArgumentMatchers.eq(1L), any())).thenReturn(List.of(token,
                FcmToken.builder().userId(1L).token("device-2").build()));
        FirebaseMessagingException failure = failure(MessagingErrorCode.UNAVAILABLE);
        when(firebase.send(any(Message.class))).thenReturn("sent-id").thenThrow(failure);

        assertSame(failure, assertThrows(FirebaseMessagingException.class, () -> sender.send(notification)));
    }

    @Test
    void successfulDeviceAndUnregisteredDeviceCompleteWithoutRetry() throws Exception {
        when(tokens.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(ArgumentMatchers.eq(1L), any())).thenReturn(List.of(token,
                FcmToken.builder().userId(1L).token("device-2").build()));
        FirebaseMessagingException failure = failure(MessagingErrorCode.UNREGISTERED);
        when(firebase.send(any(Message.class))).thenThrow(failure)
                .thenReturn("sent-id");

        assertEquals("sent-id", sender.send(notification));
        verify(tokens).delete(token);
    }

    @Test
    void storesOneProviderIdForMultipleDevices() throws Exception {
        when(tokens.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(ArgumentMatchers.eq(1L), any())).thenReturn(List.of(token, token, token));
        when(firebase.send(any(Message.class))).thenReturn("a".repeat(150), "b".repeat(150), "c".repeat(150));

        assertEquals("c".repeat(150), sender.send(notification));
    }

    @Test
    void retrySkipsDevicesWithPersistedSuccessReceipts() throws Exception {
        FcmToken second = FcmToken.builder().userId(1L).token("device-2").build();
        ReflectionTestUtils.setField(second, "id", 2L);
        when(tokens.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(ArgumentMatchers.eq(1L), any()))
                .thenReturn(List.of(token, second));
        when(firebase.send(any(Message.class))).thenReturn("first-success")
                .thenThrow(failure(MessagingErrorCode.UNAVAILABLE)).thenReturn("second-success");

        assertThrows(FirebaseMessagingException.class, () -> sender.send(notification));
        verify(receipts).saveAndFlush(ArgumentMatchers.argThat(receipt -> receipt.getTokenId().equals(1L)));
        when(receipts.existsByNotificationIdAndTokenId(notification.getId(), 1L)).thenReturn(true);

        assertEquals("second-success", sender.send(notification));
        verify(firebase, times(3)).send(any(Message.class));
    }

    @Test
    void noDeviceIsPermanentFailure() {
        when(tokens.findTop10ByUserIdAndLastSeenAtAfterOrderByLastSeenAtDesc(ArgumentMatchers.eq(1L), any())).thenReturn(List.of());
        assertThrows(PermanentSendFailureException.class, () -> sender.send(notification));
    }

    @Test
    void unknownErrorRemainsRetryable() throws Exception {
        FirebaseMessagingException failure = failure(null);
        when(firebase.send(any(Message.class))).thenThrow(failure);
        assertSame(failure, assertThrows(FirebaseMessagingException.class, () -> sender.send(notification)));
    }

    private FirebaseMessagingException failure(MessagingErrorCode code) {
        FirebaseMessagingException failure = mock(FirebaseMessagingException.class);
        when(failure.getMessagingErrorCode()).thenReturn(code);
        return failure;
    }
}
