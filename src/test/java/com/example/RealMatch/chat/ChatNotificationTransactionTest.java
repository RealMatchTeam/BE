package com.example.RealMatch.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.business.application.event.CampaignApplyStatusChangedEvent;
import com.example.RealMatch.business.application.event.CampaignProposalSentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalStatusChangedEvent;
import com.example.RealMatch.business.domain.enums.ProposalDirection;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.chat.application.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.application.dto.websocket.ChatSendMessageCommand;
import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.event.CollaborationChatEventListener;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.service.message.ChatMessageCommandService;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.application.util.JacksonSystemMessagePayloadSerializer;
import com.example.RealMatch.chat.application.util.MessagePreviewGenerator;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.infrastructure.persistence.JpaChatMessageRepository;
import com.example.RealMatch.chat.infrastructure.persistence.JpaChatRoomMemberRepository;
import com.example.RealMatch.chat.infrastructure.persistence.JpaChatRoomRepository;
import com.example.RealMatch.chat.infrastructure.tx.SpringAfterCommitExecutor;
import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.application.event.NotificationEventListener;
import com.example.RealMatch.notification.application.service.NotificationChannelResolver;
import com.example.RealMatch.notification.application.service.NotificationMessageTemplateService;
import com.example.RealMatch.notification.application.service.NotificationService;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationDeliveryRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationOutboxRepository;
import com.example.RealMatch.notification.infrastructure.persistence.JpaNotificationRepository;
import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.NotificationSettingRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/isolated-chat-notification-tests.yml",
        "spring.datasource.url=jdbc:h2:mem:chat_reliability;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = ChatNotificationTransactionTest.JpaConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ChatNotificationTransactionTest {

    @Configuration
    @EnableRetry(proxyTargetClass = true)
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = {JpaChatRoomRepository.class, JpaNotificationRepository.class, UserRepository.class})
    @EntityScan(basePackageClasses = {ChatMessage.class, Notification.class, User.class})
    @Import({ChatRoomCommandService.class, ChatRoomMemberService.class,
            ChatMessageCommandService.class, ChatMessageSocketService.class, ChatMessageResponseMapper.class,
            JacksonSystemMessagePayloadSerializer.class, MessagePreviewGenerator.class, SpringAfterCommitExecutor.class,
            CollaborationChatEventListener.class,
            NotificationEventListener.class, NotificationService.class, NotificationChannelResolver.class,
            NotificationMessageTemplateService.class})
    static class JpaConfig {
        @Bean
        JPAQueryFactory queryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }

        @Bean
        ChatMessageEventPublisher messagePublisher() {
            return mock(ChatMessageEventPublisher.class);
        }

        @Bean
        AttachmentQueryService attachmentQueryService() {
            return mock(AttachmentQueryService.class);
        }

        @Bean
        BrandRepository brandRepository() {
            return mock(BrandRepository.class);
        }

        @Bean
        MatchedCampaignPayloadProvider matchedCampaignPayloadProvider() {
            return mock(MatchedCampaignPayloadProvider.class);
        }
    }

    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ApplicationEventPublisher events;
    @Autowired
    private UserRepository users;
    @Autowired
    private NotificationSettingRepository settings;
    @Autowired
    private BrandRepository brands;
    @Autowired
    private ChatRoomCommandService roomService;
    @Autowired
    private ChatMessageSocketService socketService;
    @Autowired
    private JpaChatMessageRepository messages;
    @Autowired
    private JpaChatRoomRepository rooms;
    @Autowired
    private JpaChatRoomMemberRepository members;
    @Autowired
    private JpaNotificationRepository notifications;
    @Autowired
    private JpaNotificationDeliveryRepository deliveries;
    @Autowired
    private JpaNotificationOutboxRepository outboxes;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ChatMessageEventPublisher broadcasts;
    @Autowired
    private AttachmentQueryService attachments;
    @Autowired
    private MatchedCampaignPayloadProvider matchedPayloads;
    private TransactionTemplate tx;
    private User brandUser;
    private User creator;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
        reset(brands, broadcasts, attachments, matchedPayloads);
        brandUser = users.saveAndFlush(User.builder().email("brand@example.com").role(Role.BRAND).build());
        creator = users.saveAndFlush(User.builder().email("creator@example.com").role(Role.CREATOR).nickname("before").build());
        Brand brand = mock(Brand.class);
        when(brand.getBrandName()).thenReturn("Brand");
        when(brands.findByUserId(brandUser.getId())).thenReturn(Optional.of(brand));
    }

    @Test
    void businessChatInboxAndOutboxCommitTogetherAndReproposalHasItsOwnIdentity() {
        settings.saveAndFlush(NotificationSetting.builder().user(creator).appPushEnabled(true).emailEnabled(true).build());
        CampaignProposalSentEvent first = event("first-" + UUID.randomUUID());
        CampaignProposalSentEvent second = event("second-" + UUID.randomUUID());
        tx.executeWithoutResult(status -> events.publishEvent(first));
        Long roomId = roomService.createOrGetRoomSystem(brandUser.getId(), creator.getId()).roomId();
        long deliveryCount = deliveries.count();
        long outboxCount = outboxes.count();
        assertEquals(2, notificationsForCreatorDeliveryCount());
        clearInvocations(broadcasts);

        tx.executeWithoutResult(status -> events.publishEvent(first));
        assertEquals(deliveryCount, deliveries.count());
        assertEquals(outboxCount, outboxes.count());
        verify(broadcasts, never()).publishMessageCreated(any(), any());

        tx.executeWithoutResult(status -> events.publishEvent(second));
        assertEquals(2, messages.findMessagesByRoomId(roomId, null, 20).size());
        assertEquals(deliveryCount + 2, deliveries.count());
        assertEquals(outboxCount + 2, outboxes.count());
    }

    @Test
    void failureBeforeCommitRollsBackBusinessAndPreviouslySavedChat() {
        long messageCount = messages.count();
        long notificationCount = notifications.count();
        long outboxCount = outboxes.count();
        when(brands.findByUserId(brandUser.getId())).thenAnswer(call -> {
            assertEquals(messageCount + 1, messages.count());
            throw new IllegalStateException("Template lookup failed");
        });

        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status -> {
            users.findById(creator.getId()).orElseThrow().updateInfo("changed", null, null);
            events.publishEvent(event("rollback-" + UUID.randomUUID()));
        }));

        assertEquals("before", users.findById(creator.getId()).orElseThrow().getNickname());
        assertEquals(messageCount, messages.count());
        assertEquals(notificationCount, notifications.count());
        assertEquals(outboxCount, outboxes.count());
        assertTrue(rooms.findByRoomKey("direct:" + brandUser.getId() + ":" + creator.getId()).isEmpty());
        verify(broadcasts, never()).publishMessageCreated(any(), any());
    }

    @Test
    void disabledChannelsAndDeletedInboxRemainIdempotent() {
        CampaignProposalSentEvent event = event("disabled-" + UUID.randomUUID());
        tx.executeWithoutResult(status -> events.publishEvent(event));
        String key = event.eventId() + ":PROPOSAL_RECEIVED:" + creator.getId();
        Notification created = tx.execute(status -> notifications.findByIdempotencyKeyIncludingDeleted(key).orElseThrow());
        notificationService.softDelete(creator.getId(), created.getId());
        long count = notifications.count();
        tx.executeWithoutResult(status -> events.publishEvent(event));
        assertEquals(count, notifications.count());
        Notification replayed = tx.execute(status -> notifications.findByIdempotencyKeyIncludingDeleted(key).orElseThrow());
        assertEquals(created.getId(), replayed.getId());
        assertTrue(replayed.isDeleted());
    }

    @Test
    void simultaneousDuplicateMessagesRetryOutsideFailedTransactionAndBroadcastOnce() throws Exception {
        Long roomId = roomService.createOrGetRoomAsMember(brandUser.getId(), brandUser.getId(), creator.getId()).roomId();
        clearInvocations(broadcasts);
        CyclicBarrier barrier = new CyclicBarrier(2);
        ChatSendMessageCommand command = new ChatSendMessageCommand(
                roomId, ChatMessageType.TEXT, "hello", null, UUID.randomUUID().toString());

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                barrier.await(5, TimeUnit.SECONDS); return socketService.sendMessage(command, creator.getId());
            });
            var second = executor.submit(() -> {
                barrier.await(5, TimeUnit.SECONDS); return socketService.sendMessage(command, creator.getId());
            });
            assertEquals(first.get(10, TimeUnit.SECONDS).messageId(), second.get(10, TimeUnit.SECONDS).messageId());
        }

        assertEquals(1, messages.findMessagesByRoomId(roomId, null, 20).size());
        verify(broadcasts).publishMessageCreated(any(), any());
    }

    @Test
    void concurrentRoomCreationReturnsOneRoomWithTwoMembers() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> roomService.createOrGetRoomSystem(brandUser.getId(), creator.getId()));
            var second = executor.submit(() -> roomService.createOrGetRoomSystem(brandUser.getId(), creator.getId()));
            Long roomId = first.get(10, TimeUnit.SECONDS).roomId();
            assertEquals(roomId, second.get(10, TimeUnit.SECONDS).roomId());
            assertEquals(2, members.findByRoomId(roomId).size());
        }
    }

    @Test
    void matchingCardsAndApplyEventsPersistWithinTheBusinessTransaction() {
        tx.executeWithoutResult(status -> events.publishEvent(event("sent-" + UUID.randomUUID())));
        Long roomId = roomService.createOrGetRoomSystem(brandUser.getId(), creator.getId()).roomId();
        when(matchedPayloads.getPayload(7L, ChatProposalDirection.BRAND_TO_CREATOR)).thenReturn(Optional.of(
                new ChatMatchedCampaignPayloadResponse(7L, "Matched", 1000L, "KRW", "", null,
                        ChatProposalDirection.BRAND_TO_CREATOR)));
        CampaignProposalStatusChangedEvent matched = new CampaignProposalStatusChangedEvent(
                10L, 7L, brandUser.getId(), creator.getId(), ProposalStatus.MATCHED, creator.getId(),
                ProposalDirection.BRAND_TO_CREATOR);
        tx.executeWithoutResult(status -> events.publishEvent(matched));
        assertEquals(3, messages.findMessagesByRoomId(roomId, null, 20).size());
        long notificationCount = notifications.count();
        tx.executeWithoutResult(status -> events.publishEvent(matched));
        assertEquals(3, messages.findMessagesByRoomId(roomId, null, 20).size());
        assertEquals(notificationCount, notifications.count());

        CampaignApplySentEvent apply = new CampaignApplySentEvent(
                20L, 7L, creator.getId(), brandUser.getId(), "Campaign", "Description", "Reason");
        CampaignApplyStatusChangedEvent changed = new CampaignApplyStatusChangedEvent(
                20L, 7L, creator.getId(), brandUser.getId(), ProposalStatus.REJECTED, brandUser.getId());
        tx.executeWithoutResult(status -> {
            events.publishEvent(apply);
            events.publishEvent(changed);
        });
        assertEquals(5, messages.findMessagesByRoomId(roomId, null, 20).size());
    }

    @Test
    void concurrentInboxCreationWithoutChannelsCreatesOneLedgerEntry() throws Exception {
        CreateNotificationCommand command = CreateNotificationCommand.builder().eventId(UUID.randomUUID().toString())
                .userId(creator.getId()).kind(NotificationKind.PROPOSAL_RECEIVED).title("Proposal").body("Body").build();
        long count = notifications.count();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> notificationService.create(command));
            var second = executor.submit(() -> notificationService.create(command));
            assertEquals(first.get(10, TimeUnit.SECONDS).getId(), second.get(10, TimeUnit.SECONDS).getId());
        }
        assertEquals(count + 1, notifications.count());
    }

    private long notificationsForCreatorDeliveryCount() {
        return deliveries.findAll().stream().filter(d -> notifications.findById(d.getNotificationId())
                .map(n -> n.getUserId().equals(creator.getId())).orElse(false)).count();
    }

    private CampaignProposalSentEvent event(String eventId) {
        return new CampaignProposalSentEvent(eventId, 10L, brandUser.getId(), brandUser.getId(), creator.getId(),
                null, "Campaign", "Summary", ProposalStatus.REVIEWING, ProposalDirection.BRAND_TO_CREATOR, true);
    }
}
