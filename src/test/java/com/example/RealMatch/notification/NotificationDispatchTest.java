package com.example.RealMatch.notification;

import static com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus.FAILED;
import static com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus.RETRY;
import static com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus.SENT;
import static com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataAccessResourceFailureException;

import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.application.exception.PermanentSendFailureException;
import com.example.RealMatch.notification.application.port.NotificationChannelSender;
import com.example.RealMatch.notification.application.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.application.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.application.repository.NotificationRepository;
import com.example.RealMatch.notification.application.service.NotificationChannelResolver;
import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.application.service.NotificationDispatchService;
import com.example.RealMatch.notification.application.service.NotificationService;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;

class NotificationDispatchTest {

    private final java.util.List<NotificationDispatchService> dispatchers = new java.util.ArrayList<>();

    @org.junit.jupiter.api.AfterEach
    void closeDispatchers() {
        dispatchers.forEach(NotificationDispatchService::close);
    }

    private final NotificationSettingRepository settings = mock(NotificationSettingRepository.class);
    private final NotificationChannelResolver resolver = new NotificationChannelResolver(settings);
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final NotificationDeliveryClaimService claims = mock(NotificationDeliveryClaimService.class);
    private final NotificationChannelSender sender = mock(NotificationChannelSender.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UUID deliveryId = UUID.randomUUID();
    private final UUID notificationId = UUID.randomUUID();
    private final Notification notification = Notification.builder().userId(1L).build();

    private final NotificationDeliveryClaimService.Claim claim = new NotificationDeliveryClaimService.Claim(deliveryId, notificationId, NotificationChannel.PUSH, 1);

    @ParameterizedTest
    @CsvSource({"true,true,2", "true,false,1", "false,true,1", "false,false,0"})
    void appliesPreferencesWithoutChangingChannelPolicy(boolean push, boolean email, int expectedCount) {
        setPreferences(push, email);
        Set<NotificationChannel> channels = resolver.resolveChannels(NotificationKind.PROPOSAL_RECEIVED, 1L);
        assertEquals(expectedCount, channels.size());
        assertEquals(push, channels.contains(NotificationChannel.PUSH));
        assertEquals(email, channels.contains(NotificationChannel.EMAIL));
        assertFalse(resolver.resolveChannels(NotificationKind.CHAT_MESSAGE, 1L)
                .contains(NotificationChannel.EMAIL));
        channels.clear();
        assertEquals(expectedCount, resolver.resolveChannels(NotificationKind.PROPOSAL_RECEIVED, 1L).size());
    }

    @Test
    void missingSettingsDisableExternalDelivery() {
        assertEquals(Set.of(), resolver.resolveChannels(NotificationKind.PROPOSAL_RECEIVED, 1L));
        assertFalse(resolver.isEnabled(1L, NotificationChannel.PUSH));
        assertFalse(resolver.isEnabled(1L, NotificationChannel.EMAIL));
    }

    @Test
    void disabledChannelsStillCreateInboxNotificationWithoutDeliveryOrOutbox() {
        setPreferences(false, false);
        NotificationDeliveryRepository deliveries = mock(NotificationDeliveryRepository.class);
        NotificationOutboxRepository outboxes = mock(NotificationOutboxRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(User.builder().build()));
        when(notifications.save(any(Notification.class))).thenAnswer(call -> call.getArgument(0));
        NotificationService service = new NotificationService(notifications, deliveries, outboxes, resolver, users);

        Notification created = service.create(CreateNotificationCommand.builder()
                .eventId("event-1").userId(1L).kind(NotificationKind.PROPOSAL_RECEIVED)
                .title("Proposal").body("New proposal").build());

        assertEquals(NotificationKind.PROPOSAL_RECEIVED, created.getKind());
        verify(notifications).save(created);
        verifyNoInteractions(deliveries, outboxes);
    }

    @Test
    void rechecksPreferencesWhenQueuedDeliveryIsDispatched() throws Exception {
        setPreferences(true, true);
        assertEquals(Set.of(NotificationChannel.PUSH, NotificationChannel.EMAIL),
                resolver.resolveChannels(NotificationKind.PROPOSAL_RECEIVED, 1L));
        NotificationDispatchService service = dispatchService();
        setPreferences(false, false);

        service.dispatch(deliveryId);

        verify(claims).complete(claim, SKIPPED, null);
        verify(sender, never()).send(any());
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(RETRY), any());
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(SENT), any());
    }

    @Test
    void failedPreferenceLookupDoesNotSend() throws Exception {
        NotificationDispatchService service = dispatchService();
        when(settings.findByUserId(1L)).thenThrow(new DataAccessResourceFailureException("DB unavailable"));

        service.dispatch(deliveryId);
        verify(claims).complete(claim, RETRY, "DB unavailable");
        verify(sender, never()).send(any());
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(SKIPPED), any());
    }

    @Test
    void completionDatabaseFailureDoesNotOverwriteAnAcceptedSendWithRetry() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenReturn("provider-id");
        var failure = new DataAccessResourceFailureException("Commit result unknown");
        doThrow(failure).when(claims).complete(claim, SENT, "provider-id");

        assertSame(failure, assertThrows(DataAccessResourceFailureException.class, () -> service.dispatch(deliveryId)));
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(RETRY), any());
        verify(sender).send(notification);
    }

    @Test
    void successfulSendIsRecorded() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenReturn("provider-id");

        service.dispatch(deliveryId);

        verify(claims).complete(claim, SENT, "provider-id");
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(RETRY), any());
    }

    @Test
    void transientSendFailureIsRetried() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenThrow(new IOException("temporarily unavailable"));

        service.dispatch(deliveryId);

        verify(claims).complete(claim, RETRY, "temporarily unavailable");
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(FAILED), any());
    }

    @Test
    void permanentSendFailureIsNotRetried() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenThrow(new PermanentSendFailureException("invalid recipient"));

        service.dispatch(deliveryId);

        verify(claims).complete(claim, FAILED, "invalid recipient");
        verify(claims, never()).complete(any(), ArgumentMatchers.eq(RETRY), any());
    }

    @Test
    void duplicateDeliveryDoesNotSendAgain() throws Exception {
        NotificationDispatchService service = dispatchService();
        when(claims.claim(deliveryId)).thenReturn(null);

        service.dispatch(deliveryId);

        verify(sender, never()).send(any());
        verifyNoInteractions(settings);
    }

    @Test
    void settingsReadFailurePreservesCause() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("DB unavailable");
        when(settings.findByUserId(1L)).thenThrow(failure);
        assertSame(failure, assertThrows(DataAccessResourceFailureException.class,
                () -> resolver.resolveChannels(NotificationKind.CHAT_MESSAGE, 1L)));
    }

    private void setPreferences(boolean push, boolean email) {
        when(settings.findByUserId(1L)).thenReturn(Optional.of(NotificationSetting.builder()
                .appPushEnabled(push).emailEnabled(email).build()));
    }

    private NotificationDispatchService dispatchService() {
        when(sender.getChannel()).thenReturn(NotificationChannel.PUSH);
        when(sender.isAvailable()).thenReturn(true);
        when(claims.claim(deliveryId)).thenReturn(claim);
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().build()));
        var service = new NotificationDispatchService(claims, notifications, resolver, users, List.of(sender));
        dispatchers.add(service);
        return service;
    }
}
