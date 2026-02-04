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
 * 제안(Proposal) 관련 시스템 메시지 처리를 담당하는 핸들러.
 * 
 * <p>역할: 오케스트레이션 (payload 준비, roomId 찾기, 메시지 종류 결정, 멱등성 키 결정)
 * <p>실제 전송/재시도/멱등성/DLQ는 Base의 execute 메서드와 SystemMessageRetrySender가 담당합니다.
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
        // 1. 논리적 검증 먼저 수행
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

        // 2. 멱등성 키 결정 (deterministic)
        String idempotencyKey = event.eventId(); // 이미 deterministic: "PROPOSAL_SENT:{proposalId}" 또는 "RE_PROPOSAL_SENT:{proposalId}"

        // 3. eventMeta 생성
        SystemEventMeta meta = new SystemEventMeta(
                idempotencyKey,
                event.roomId(),
                "ProposalSentEvent"
        );

        // 4. contextData 준비 (공통 키: messageKind, domainId 필수)
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", messageKind.toString());
        if (event.payload().proposalId() != null) {
            contextData.put("domainId", event.payload().proposalId()); // 공통 키
            contextData.put("proposalId", event.payload().proposalId());
        }

        // 5. Base의 execute 메서드 사용 (payload 생성은 이미 event에 있으므로 supplier로 감싸기)
        execute(
                meta,
                contextData,
                () -> event.payload(), // payload는 이미 event에 있음
                payload -> {
                    boolean sent = retrySender.sendWithIdempotency(
                            meta.idempotencyKey(),
                            meta.roomId(),
                            messageKind,
                            payload,
                            meta.eventType(),
                            contextData
                    );

                    if (sent) {
                        LOG.info("[Proposal] System message sent. roomId={}, proposalId={}, kind={}",
                                meta.roomId(), event.payload().proposalId(), messageKind);
                    } else {
                        LOG.warn("[Proposal] Duplicate event, skipped. idempotencyKey={}", meta.idempotencyKey());
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
        // 1. 채팅방 조회 (논리적 검증을 먼저 수행)
        Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                event.brandUserId(), event.creatorUserId());
        if (roomIdOpt.isEmpty()) {
            LOG.warn("[Proposal] Chat room not found. eventId={}, proposalId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.proposalId(), event.brandUserId(), event.creatorUserId());
            // 논리적 실패는 Redis 키를 남기지 않음
            return;
        }
        Long roomId = roomIdOpt.get();

        LOG.info("[Proposal] Processing status change. eventId={}, proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                event.eventId(), event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

        // 2. 채팅방 제안 상태 업데이트
        ChatProposalStatus chatStatus = event.newStatus();
        chatRoomUpdateService.updateProposalStatusByUsers(
                event.brandUserId(),
                event.creatorUserId(),
                chatStatus
        );

        // 3. 상태 변경 알림 메시지 전송 (별도 멱등성 키)
        // event.eventId()는 "PROPOSAL_STATUS_CHANGED:{proposalId}:{newStatus}" 형태로 유니크함
        // ":NOTICE"를 붙여 "PROPOSAL_STATUS_CHANGED:{proposalId}:{newStatus}:NOTICE" 형태로 구분
        String statusNoticeKey = String.format("%s:NOTICE", event.eventId());
        
        SystemEventMeta noticeMeta = new SystemEventMeta(
                statusNoticeKey,
                roomId,
                "ProposalStatusChangedEvent"
        );

        // contextData 준비 (공통 키: messageKind, domainId 필수)
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("messageKind", ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE.toString());
        if (event.proposalId() != null) {
            contextData.put("domainId", event.proposalId()); // 공통 키
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
                        LocalDateTime.now()
                ),
                payload -> {
                    retrySender.sendWithIdempotency(
                            noticeMeta.idempotencyKey(),
                            noticeMeta.roomId(),
                            ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                            payload,
                            noticeMeta.eventType(),
                            contextData
                    );
                }
        );

        // 4. 매칭 완료 시 추가 카드 전송 (별도 멱등성 키)
        // MATCHED 상태는 보통 1회만 발생하므로 proposalId만으로도 충분히 유니크함
        // event.eventId()는 "PROPOSAL_STATUS_CHANGED:{proposalId}:MATCHED" 형태
        if (event.newStatus() == ChatProposalStatus.MATCHED) {
            String matchedCardKey = String.format("%s:MATCHED_CARD", event.eventId());
            
            SystemEventMeta matchedMeta = new SystemEventMeta(
                    matchedCardKey,
                    roomId,
                    "ProposalStatusChangedEvent"
            );

            // contextData 준비 (공통 키: messageKind, domainId 필수)
            Map<String, Object> matchedContextData = new HashMap<>();
            matchedContextData.put("messageKind", ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD.toString());
            if (event.proposalId() != null) {
                matchedContextData.put("domainId", event.proposalId()); // 공통 키
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
                        retrySender.sendWithIdempotency(
                                matchedMeta.idempotencyKey(),
                                matchedMeta.roomId(),
                                ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                                payload,
                                matchedMeta.eventType(),
                                matchedContextData
                        );
                    }
            );
        }

        LOG.info("[Proposal] Status change processed. roomId={}, status={}",
                roomId, chatStatus);
    }
}
