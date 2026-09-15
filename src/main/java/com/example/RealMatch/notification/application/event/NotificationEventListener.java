package com.example.RealMatch.notification.application.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.application.event.AutoConfirmedEvent;
import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.business.application.event.CampaignCompletedEvent;
import com.example.RealMatch.business.application.event.CampaignProposalSentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalStatusChangedEvent;
import com.example.RealMatch.business.application.event.SettlementReadyEvent;
import com.example.RealMatch.business.domain.enums.ProposalDirection;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.notification.application.dto.CreateNotificationCommand;
import com.example.RealMatch.notification.application.service.NotificationMessageTemplateService;
import com.example.RealMatch.notification.application.service.NotificationMessageTemplateService.MessageTemplate;
import com.example.RealMatch.notification.application.service.NotificationService;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.entity.enums.ReferenceType;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final NotificationMessageTemplateService messageTemplateService;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;

    // ==================== CampaignProposalSentEvent ====================

    /**
     * CampaignProposalSentEvent 구독.
     * proposalDirection=BRAND_TO_CREATOR일 때 PROPOSAL_RECEIVED 알림 생성.
     */
    @Order(100)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignProposalSent(CampaignProposalSentEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid CampaignProposalSentEvent: event is null");
            return;
        }

        if (event.proposalDirection() != ProposalDirection.BRAND_TO_CREATOR) {
            LOG.debug("[Notification] Skipping notification for CREATOR_TO_BRAND proposal. proposalId={}",
                    event.proposalId());
            return;
        }

        String eventId = event.eventId();
        String brandName = findBrandNameByUserId(event.brandUserId());

        MessageTemplate template = messageTemplateService.createProposalReceivedMessage(brandName);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(event.creatorUserId())
                .kind(NotificationKind.PROPOSAL_RECEIVED)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN_PROPOSAL)
                .referenceId(String.valueOf(event.proposalId()))
                .campaignId(event.campaignId())
                .proposalId(event.proposalId())
                .build();

        notificationService.create(command);

        LOG.info("[Notification] Created PROPOSAL_RECEIVED. eventId={}, proposalId={}, userId={}",
                eventId, event.proposalId(), event.creatorUserId());
    }

    // ==================== CampaignProposalStatusChangedEvent ====================

    /**
     * CampaignProposalStatusChangedEvent 구독.
     * <ul>
     *   <li>MATCHED → 크리에이터에게 CAMPAIGN_MATCHED + 제안 보낸 사람에게 PROPOSAL_SENT(수락)</li>
     *   <li>REJECTED → 제안 보낸 사람에게 PROPOSAL_SENT(거절)</li>
     * </ul>
     * 모든 알림 생성은 업무 트랜잭션에 참여하며 실패 시 함께 롤백한다.
     */
    @Order(100)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignProposalStatusChanged(CampaignProposalStatusChangedEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid CampaignProposalStatusChangedEvent: event is null");
            return;
        }

        if (event.newStatus() == ProposalStatus.MATCHED) {
            // 1) 크리에이터에게 CAMPAIGN_MATCHED
            createCampaignMatchedNotification(event);
            // 2) 제안 보낸 사람에게 PROPOSAL_SENT (수락)
            createProposalSentNotification(event, true);
        } else if (event.newStatus() == ProposalStatus.REJECTED) {
            createProposalSentNotification(event, false);
        }
    }

    /**
     * CAMPAIGN_MATCHED 알림 생성 (크리에이터에게)
     */
    private void createCampaignMatchedNotification(CampaignProposalStatusChangedEvent event) {
        String eventId = event.eventId();
        String brandName = findBrandNameByUserId(event.brandUserId());

        MessageTemplate template = messageTemplateService.createCampaignMatchedMessage(brandName);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(event.creatorUserId())
                .kind(NotificationKind.CAMPAIGN_MATCHED)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN_PROPOSAL)
                .referenceId(String.valueOf(event.proposalId()))
                .campaignId(event.campaignId())
                .proposalId(event.proposalId())
                .build();

        notificationService.create(command);

        LOG.info("[Notification] Created CAMPAIGN_MATCHED. eventId={}, proposalId={}, userId={}",
                eventId, event.proposalId(), event.creatorUserId());
    }

    /**
     * PROPOSAL_SENT 알림 생성 (제안 보낸 사람에게, 수락/거절 공통)
     */
    private void createProposalSentNotification(CampaignProposalStatusChangedEvent event, boolean isAccepted) {
        String eventId = event.eventId();
        // proposalDirection을 이용해 senderUserId 결정 (DB 조회 불필요)
        Long senderUserId = event.proposalDirection() == ProposalDirection.BRAND_TO_CREATOR
                ? event.brandUserId()
                : event.creatorUserId();

        String brandName = findBrandNameByUserId(event.brandUserId());

        MessageTemplate template = messageTemplateService.createProposalSentMessage(brandName, isAccepted);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(senderUserId)
                .kind(NotificationKind.PROPOSAL_SENT)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN_PROPOSAL)
                .referenceId(String.valueOf(event.proposalId()))
                .campaignId(event.campaignId())
                .proposalId(event.proposalId())
                .build();

        notificationService.create(command);

        String resultLabel = isAccepted ? "accepted" : "rejected";
        LOG.info("[Notification] Created PROPOSAL_SENT ({}). eventId={}, proposalId={}, userId={}",
                resultLabel, eventId, event.proposalId(), senderUserId);
    }

    // ==================== CampaignApplySentEvent ====================

    /**
     * CampaignApplySentEvent 구독.
     * 브랜드에게 CAMPAIGN_APPLIED 알림 생성.
     */
    @Order(100)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignApplySent(CampaignApplySentEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid CampaignApplySentEvent: event is null");
            return;
        }

        String eventId = event.eventId();
        User creator = userRepository.findById(event.creatorUserId())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found: " + event.creatorUserId()));
        String creatorName = resolveDisplayName(creator);

        MessageTemplate template = messageTemplateService.createCampaignAppliedMessage(creatorName);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(event.brandUserId())
                .kind(NotificationKind.CAMPAIGN_APPLIED)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN_APPLY)
                .referenceId(String.valueOf(event.applyId()))
                .campaignId(event.campaignId())
                .build();

        notificationService.create(command);

        LOG.info("[Notification] Created CAMPAIGN_APPLIED. eventId={}, applyId={}, userId={}",
                eventId, event.applyId(), event.brandUserId());
    }

    // ==================== 데모데이 이후용: CampaignCompletedEvent ====================

    /**
     * CampaignCompletedEvent 구독.
     * 크리에이터에게 CAMPAIGN_COMPLETED 알림 생성.
     * (데모데이 이후 해당 플로우에서 이벤트 발행만 추가하면 동작)
     */
    @Order(100)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCampaignCompleted(CampaignCompletedEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid CampaignCompletedEvent: event is null");
            return;
        }

        String eventId = String.format("CAMPAIGN_COMPLETED:%d", event.campaignId());
        String brandName = findBrandNameByUserId(event.brandUserId());

        MessageTemplate template = messageTemplateService.createCampaignCompletedMessage(brandName);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(event.creatorUserId())
                .kind(NotificationKind.CAMPAIGN_COMPLETED)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN)
                .referenceId(String.valueOf(event.campaignId()))
                .campaignId(event.campaignId())
                .build();

        notificationService.create(command);

        LOG.info("[Notification] Created CAMPAIGN_COMPLETED. eventId={}, campaignId={}, userId={}",
                eventId, event.campaignId(), event.creatorUserId());
    }

    // ==================== 데모데이 이후용: SettlementReadyEvent ====================

    /**
     * SettlementReadyEvent 구독.
     * 크리에이터에게 SETTLEMENT_READY 알림 생성.
     * (데모데이 이후 해당 플로우에서 이벤트 발행만 추가하면 동작)
     */
    @Order(100)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleSettlementReady(SettlementReadyEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid SettlementReadyEvent: event is null");
            return;
        }

        String eventId = String.format("SETTLEMENT_READY:%d", event.campaignId());
        String brandName = findBrandNameByUserId(event.brandUserId());

        MessageTemplate template = messageTemplateService.createSettlementReadyMessage(brandName);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(event.creatorUserId())
                .kind(NotificationKind.SETTLEMENT_READY)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN)
                .referenceId(String.valueOf(event.campaignId()))
                .campaignId(event.campaignId())
                .build();

        notificationService.create(command);

        LOG.info("[Notification] Created SETTLEMENT_READY. eventId={}, campaignId={}, userId={}",
                eventId, event.campaignId(), event.creatorUserId());
    }

    // ==================== 데모데이 이후용: AutoConfirmedEvent ====================

    /**
     * AutoConfirmedEvent 구독.
     * 브랜드에게 AUTO_CONFIRMED 알림 생성.
     * (데모데이 이후 해당 플로우에서 이벤트 발행만 추가하면 동작)
     */
    @Order(100)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAutoConfirmed(AutoConfirmedEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid AutoConfirmedEvent: event is null");
            return;
        }

        String eventId = String.format("AUTO_CONFIRMED:%d", event.campaignId());
        String brandName = findBrandNameByUserId(event.brandUserId());

        MessageTemplate template = messageTemplateService.createAutoConfirmedMessage(brandName);

        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .eventId(eventId)
                .userId(event.brandUserId())
                .kind(NotificationKind.AUTO_CONFIRMED)
                .title(template.title())
                .body(template.body())
                .referenceType(ReferenceType.CAMPAIGN)
                .referenceId(String.valueOf(event.campaignId()))
                .campaignId(event.campaignId())
                .build();

        notificationService.create(command);

        LOG.info("[Notification] Created AUTO_CONFIRMED. eventId={}, campaignId={}, userId={}",
                eventId, event.campaignId(), event.brandUserId());
    }

    // ==================== 공통 헬퍼 ====================

    /**
     * brandUserId(User PK)로 Brand를 조회하여 brandName을 반환한다.
     */
    private String findBrandNameByUserId(Long brandUserId) {
        Brand brand = brandRepository.findByUserId(brandUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "Brand not found for userId: " + brandUserId));
        return brand.getBrandName();
    }

    /**
     * User의 표시 이름을 결정한다. (nickname 우선, 없으면 name)
     */
    private String resolveDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            return user.getNickname();
        }
        return user.getName();
    }
}
