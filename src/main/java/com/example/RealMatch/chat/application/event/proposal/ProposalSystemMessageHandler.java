package com.example.RealMatch.chat.application.event.proposal;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.BaseSystemMessageHandler;
import com.example.RealMatch.chat.application.event.SystemEventMeta;
import com.example.RealMatch.chat.application.event.sender.SystemMessageRetrySender;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;

/**
 * 제안(Proposal) 시스템 메시지 이벤트 핸들러.
 *
 * <p>이벤트에 따라 적절한 시스템 메시지를 구성하고 전송을 위임합니다.
 * 메시지 전송, 재시도, 멱등성, DLQ 처리는 공통 컴포넌트에 위임되며,
 * sendWithIdempotency의 반환값은 메시지 처리 성공 여부를 의미합니다.
 * - true: 전송 성공 및 markAsProcessed 완료 (at-least-once 보장)
 * - false: 중복 이벤트 또는 논리적 실패로 처리 중단
 */
@Component
public class ProposalSystemMessageHandler extends BaseSystemMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalSystemMessageHandler.class);

    private final ChatRoomUpdateService chatRoomUpdateService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final MatchedCampaignPayloadProvider matchedCampaignPayloadProvider;

    public ProposalSystemMessageHandler(
            FailedEventDlq failedEventDlq,
            SystemMessageRetrySender retrySender,
            ChatRoomUpdateService chatRoomUpdateService,
            ChatRoomQueryService chatRoomQueryService,
            MatchedCampaignPayloadProvider matchedCampaignPayloadProvider
    ) {
        super(failedEventDlq, retrySender);
        this.chatRoomUpdateService = chatRoomUpdateService;
        this.chatRoomQueryService = chatRoomQueryService;
        this.matchedCampaignPayloadProvider = matchedCampaignPayloadProvider;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * 제안 전송 이벤트 처리
     * PROPOSAL_CARD 또는 RE_PROPOSAL_CARD 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleProposalSent(ProposalSentEvent event) {
        if (event.roomId() == null || event.payload() == null) {
            LOG.warn("[Proposal] Invalid event. eventId={}, roomId={}, payload={}",
                    event.eventId(), event.roomId(), event.payload());
            return;
        }

        LOG.info("[Proposal] Processing proposal sent. eventId={}, roomId={}, proposalId={}, isReProposal={}",
                event.eventId(), event.roomId(), event.payload().proposalId(), event.isReProposal());

        ChatSystemMessageKind messageKind = event.isReProposal()
                ? ChatSystemMessageKind.RE_PROPOSAL_CARD
                : ChatSystemMessageKind.PROPOSAL_CARD;

        // 멱등성 키 결정 (deterministic: "PROPOSAL_SENT:{proposalId}" or "RE_PROPOSAL_SENT:{proposalId}")
        String idempotencyKey = event.eventId();

        // 이벤트 메타데이터 생성
        SystemEventMeta meta = new SystemEventMeta(
                idempotencyKey,
                event.roomId(),
                "ProposalSentEvent"
        );

        // 컨텍스트 데이터 준비
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", messageKind.toString());
        if (event.payload().proposalId() != null) {
            contextData.put("domainId", event.payload().proposalId());
            contextData.put("proposalId", event.payload().proposalId());
        }

        execute(
                meta,
                contextData,
                () -> event.payload(),
                payload -> {
                    boolean accepted = retrySender.sendWithIdempotency(
                            meta.idempotencyKey(),
                            meta.roomId(),
                            messageKind,
                            payload,
                            meta.eventType(),
                            contextData
                    );

                    if (accepted) {
                        LOG.info("[Proposal] System message event accepted. idempotencyKey={}, roomId={}, proposalId={}, kind={}",
                                meta.idempotencyKey(), meta.roomId(), event.payload().proposalId(), messageKind);
                    } else {
                        LOG.warn("[Proposal] System message event skipped (duplicate or logical failure). idempotencyKey={}, roomId={}, proposalId={}, kind={}",
                                meta.idempotencyKey(), meta.roomId(), event.payload().proposalId(), messageKind);
                    }
                }
        );
    }

    /**
     * 제안 상태 변경 이벤트 처리
     * PROPOSAL_STATUS_NOTICE 시스템 메시지를 전송하고, 매칭 시 MATCHED_CAMPAIGN_CARD도 전송합니다.
     *
     * <p>MATCHED인 경우 statusNotice와 matchedCard는 각각 별도의 멱등성 키를 사용합니다.
     */
    @Async
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                event.brandUserId(), event.creatorUserId());
        if (roomIdOpt.isEmpty()) {
            LOG.warn("[Proposal] Chat room not found. eventId={}, proposalId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.proposalId(), event.brandUserId(), event.creatorUserId());
            return;
        }
        Long roomId = roomIdOpt.get();

        LOG.info("[Proposal] Processing status change. eventId={}, proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                event.eventId(), event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

        // 도메인 상태 업데이트 (시스템 메시지 성공/실패와 무관)
        ChatProposalStatus chatStatus = event.newStatus();
        chatRoomUpdateService.updateProposalStatusByUsers(
                event.brandUserId(),
                event.creatorUserId(),
                chatStatus
        );

        // 상태 변경 알림 메시지 전송
        String statusNoticeKey = String.format("%s:NOTICE", event.eventId());

        SystemEventMeta noticeMeta = new SystemEventMeta(
                statusNoticeKey,
                roomId,
                "ProposalStatusChangedEvent"
        );

        // 컨텍스트 데이터 준비
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE.toString());
        if (event.proposalId() != null) {
            contextData.put("domainId", event.proposalId());
            contextData.put("proposalId", event.proposalId());
        }
        if (event.actorUserId() != null) {
            contextData.put("actorUserId", event.actorUserId());
        }

        execute(
                noticeMeta,
                contextData,
                () -> new ChatProposalStatusNoticePayloadResponse(
                        event.proposalId(),
                        event.actorUserId(),
                        LocalDateTime.now(),
                        event.newStatus()
                ),
                payload -> {
                    boolean accepted = retrySender.sendWithIdempotency(
                            noticeMeta.idempotencyKey(),
                            noticeMeta.roomId(),
                            ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                            payload,
                            noticeMeta.eventType(),
                            contextData
                    );

                    if (!accepted) {
                        LOG.warn("[Proposal] Status notice event skipped (duplicate or logical failure). idempotencyKey={}, proposalId={}, status={}",
                                noticeMeta.idempotencyKey(), event.proposalId(), event.newStatus());
                    }
                }
        );

        // 매칭 완료 시 추가 카드 전송
        if (event.newStatus() == ChatProposalStatus.MATCHED) {
            String matchedCardKey = String.format("%s:MATCHED_CARD", event.eventId());

            SystemEventMeta matchedMeta = new SystemEventMeta(
                    matchedCardKey,
                    roomId,
                    "ProposalStatusChangedEvent"
            );

            // 컨텍스트 데이터 준비
            Map<String, Object> matchedContextData = new HashMap<>();
            matchedContextData.put("messageKind", ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD.toString());
            if (event.proposalId() != null) {
                matchedContextData.put("domainId", event.proposalId());
                matchedContextData.put("proposalId", event.proposalId());
            }
            if (event.campaignId() != null) {
                matchedContextData.put("campaignId", event.campaignId());
            }

            execute(
                    matchedMeta,
                    matchedContextData,
                    () -> matchedCampaignPayloadProvider.getPayload(event.campaignId())
                            .orElseThrow(() -> {
                                String message = String.format(
                                        "Failed to get matched campaign payload. campaignId=%d may not exist or be deleted",
                                        event.campaignId()
                                );
                                LOG.warn("[Proposal] {}. eventId={}, roomId={}", message, event.eventId(), roomId);
                                return new IllegalStateException(message);
                            }),
                    payload -> {
                        boolean accepted = retrySender.sendWithIdempotency(
                                matchedMeta.idempotencyKey(),
                                matchedMeta.roomId(),
                                ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                                payload,
                                matchedMeta.eventType(),
                                matchedContextData
                        );

                        if (!accepted) {
                            LOG.warn("[Proposal] Matched card event skipped (duplicate or logical failure). idempotencyKey={}, proposalId={}, campaignId={}",
                                    matchedMeta.idempotencyKey(), event.proposalId(), event.campaignId());
                        }
                    }
            );
        }

        LOG.info("[Proposal] Status change processed. roomId={}, status={}", roomId, chatStatus);
    }
}
