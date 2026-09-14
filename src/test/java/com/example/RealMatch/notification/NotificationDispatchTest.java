package com.example.RealMatch.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.dao.DataAccessResourceFailureException;

import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.application.exception.PermanentSendFailureException;
import com.example.RealMatch.notification.application.port.NotificationChannelSender;
import com.example.RealMatch.notification.application.service.NotificationChannelResolver;
import com.example.RealMatch.notification.application.service.NotificationDeliveryClaimService;
import com.example.RealMatch.notification.application.service.NotificationDispatchService;
import com.example.RealMatch.notification.application.service.NotificationService;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationOutboxRepository;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;

class NotificationDispatchTest {

    private final NotificationSettingRepository settings = mock(NotificationSettingRepository.class);
    private final NotificationChannelResolver resolver = new NotificationChannelResolver(settings);
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final NotificationDeliveryClaimService claims = mock(NotificationDeliveryClaimService.class);
    private final NotificationChannelSender sender = mock(NotificationChannelSender.class);
    private final UUID deliveryId = UUID.randomUUID();
    private final UUID notificationId = UUID.randomUUID();
    private final Notification notification = Notification.builder().userId(1L).build();

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

        service.dispatch(deliveryId, notificationId, NotificationChannel.PUSH);

        verify(claims).skipDelivery(deliveryId);
        verify(sender, never()).send(any());
        verify(claims, never()).recordFailure(any(), any());
        verify(claims, never()).markSent(any(), any());
    }

    @Test
    void failedPreferenceLookupDoesNotSend() throws Exception {
        NotificationDispatchService service = dispatchService();
        when(settings.findByUserId(1L)).thenThrow(new DataAccessResourceFailureException("DB unavailable"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.dispatch(deliveryId, notificationId, NotificationChannel.PUSH));
        verify(sender, never()).send(any());
        verify(claims, never()).skipDelivery(any());
    }

    @Test
    void successfulSendIsRecorded() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenReturn("provider-id");

        service.dispatch(deliveryId, notificationId, NotificationChannel.PUSH);

        verify(claims).markSent(deliveryId, "provider-id");
        verify(claims, never()).recordFailure(any(), any());
    }

    @Test
    void transientSendFailureIsRetried() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenThrow(new IOException("temporarily unavailable"));

        service.dispatch(deliveryId, notificationId, NotificationChannel.PUSH);

        verify(claims).recordFailure(deliveryId, "temporarily unavailable");
        verify(claims, never()).markPermanentlyFailed(any(), any());
    }

    @Test
    void permanentSendFailureIsNotRetried() throws Exception {
        setPreferences(true, false);
        NotificationDispatchService service = dispatchService();
        when(sender.send(notification)).thenThrow(new PermanentSendFailureException("invalid recipient"));

        service.dispatch(deliveryId, notificationId, NotificationChannel.PUSH);

        verify(claims).markPermanentlyFailed(deliveryId, "invalid recipient");
        verify(claims, never()).recordFailure(any(), any());
    }

    @Test
    void duplicateDeliveryDoesNotSendAgain() throws Exception {
        NotificationDispatchService service = dispatchService();
        when(claims.claimDelivery(deliveryId)).thenReturn(false);

        service.dispatch(deliveryId, notificationId, NotificationChannel.PUSH);

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
        when(claims.claimDelivery(deliveryId)).thenReturn(true);
        when(notifications.findById(notificationId)).thenReturn(Optional.of(notification));
        return new NotificationDispatchService(claims, notifications, resolver, List.of(sender));
    }
}
