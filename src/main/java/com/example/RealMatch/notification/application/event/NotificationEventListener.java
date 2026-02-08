package com.example.RealMatch.notification.application.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalSentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalStatusChangedEvent;
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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

        try {
            String eventId = generateProposalSentEventId(event.proposalId(), event.isReProposal());
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
        } catch (Exception e) {
            LOG.error("[Notification] Failed to create PROPOSAL_RECEIVED. proposalId={}, userId={}",
                    event.proposalId(), event.creatorUserId(), e);
        }
    }

    // ==================== CampaignProposalStatusChangedEvent ====================

    /**
     * CampaignProposalStatusChangedEvent 구독.
     * <ul>
     *   <li>MATCHED → 크리에이터에게 CAMPAIGN_MATCHED + 제안 보낸 사람에게 PROPOSAL_SENT(수락)</li>
     *   <li>REJECTED → 제안 보낸 사람에게 PROPOSAL_SENT(거절)</li>
     * </ul>
     * 각 알림 생성은 독립적으로 예외 처리한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCampaignProposalStatusChanged(CampaignProposalStatusChangedEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid CampaignProposalStatusChangedEvent: event is null");
            return;
        }

        if (event.newStatus() == ProposalStatus.MATCHED) {
            // 1) 크리에이터에게 CAMPAIGN_MATCHED
            createCampaignMatchedNotification(event);
            // 2) 제안 보낸 사람에게 PROPOSAL_SENT (수락) — 독립 try-catch
            createProposalSentNotification(event, true);
        } else if (event.newStatus() == ProposalStatus.REJECTED) {
            createProposalSentNotification(event, false);
        }
    }

    /**
     * CAMPAIGN_MATCHED 알림 생성 (크리에이터에게)
     */
    private void createCampaignMatchedNotification(CampaignProposalStatusChangedEvent event) {
        try {
            String eventId = generateProposalStatusChangedEventId(event.proposalId(), event.newStatus());
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
        } catch (Exception e) {
            LOG.error("[Notification] Failed to create CAMPAIGN_MATCHED. proposalId={}, userId={}",
                    event.proposalId(), event.creatorUserId(), e);
        }
    }

    /**
     * PROPOSAL_SENT 알림 생성 (제안 보낸 사람에게, 수락/거절 공통)
     */
    private void createProposalSentNotification(CampaignProposalStatusChangedEvent event, boolean isAccepted) {
        try {
            String eventId = generateProposalStatusChangedEventId(event.proposalId(), event.newStatus());
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
        } catch (Exception e) {
            LOG.error("[Notification] Failed to create PROPOSAL_SENT. proposalId={}, newStatus={}",
                    event.proposalId(), event.newStatus(), e);
        }
    }

    // ==================== CampaignApplySentEvent ====================

    /**
     * CampaignApplySentEvent 구독.
     * 브랜드에게 CAMPAIGN_APPLIED 알림 생성.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCampaignApplySent(CampaignApplySentEvent event) {
        if (event == null) {
            LOG.warn("[Notification] Invalid CampaignApplySentEvent: event is null");
            return;
        }

        try {
            String eventId = generateApplySentEventId(event.applyId());
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
        } catch (Exception e) {
            LOG.error("[Notification] Failed to create CAMPAIGN_APPLIED. applyId={}, userId={}",
                    event.applyId(), event.brandUserId(), e);
        }
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

    // ==================== EventId 생성 (멱등성 보장용) ====================

    /**
     * CampaignProposalSentEvent의 결정적 eventId 생성
     */
    private String generateProposalSentEventId(Long proposalId, boolean isReProposal) {
        String type = isReProposal ? "RE_PROPOSAL_SENT" : "PROPOSAL_SENT";
        return String.format("%s:%d", type, proposalId);
    }

    /**
     * CampaignProposalStatusChangedEvent의 결정적 eventId 생성
     */
    private String generateProposalStatusChangedEventId(Long proposalId, ProposalStatus newStatus) {
        return String.format("PROPOSAL_STATUS_CHANGED:%d:%s", proposalId, newStatus);
    }

    /**
     * CampaignApplySentEvent의 결정적 eventId 생성
     */
    private String generateApplySentEventId(Long applyId) {
        return String.format("APPLY_SENT:%d", applyId);
    }
}
