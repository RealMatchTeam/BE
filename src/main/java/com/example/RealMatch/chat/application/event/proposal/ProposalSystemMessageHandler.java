package com.example.RealMatch.chat.application.event.proposal;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.event.sender.SystemMessageRetrySender;
import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

/**
 * 제안(Proposal) 관련 시스템 메시지 처리를 담당하는 핸들러.
 * 
 * <p>역할: 오케스트레이션 (payload 준비, roomId 찾기, 메시지 종류 결정, 멱등성 키 결정)
 * <p>실제 전송/재시도/멱등성/DLQ는 SystemMessageRetrySender가 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class ProposalSystemMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalSystemMessageHandler.class);

    private final SystemMessageRetrySender retrySender;
    private final ChatRoomUpdateService chatRoomUpdateService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final MatchedCampaignPayloadProvider matchedCampaignPayloadProvider;

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

        // 3. RetrySender로 전송 (멱등성 체크 + 재시도 + DLQ 포함)
        Map<String, Object> additionalData = new HashMap<>();
        if (event.payload().proposalId() != null) {
            additionalData.put("proposalId", event.payload().proposalId());
        }
        additionalData.put("messageKind", messageKind.toString());

        try {
            boolean sent = retrySender.sendWithIdempotency(
                    idempotencyKey,
                    event.roomId(),
                    messageKind,
                    event.payload(),
                    "ProposalSentEvent",
                    additionalData
            );

            if (sent) {
                LOG.info("[Proposal] System message sent. roomId={}, proposalId={}, kind={}",
                        event.roomId(), event.payload().proposalId(), messageKind);
            } else {
                LOG.warn("[Proposal] Duplicate event, skipped. eventId={}", event.eventId());
            }
        } catch (IllegalArgumentException ex) {
            // 논리적 실패는 RetrySender에서 이미 처리됨
            LOG.warn("[Proposal] Logical failure. eventId={}, error={}", event.eventId(), ex.getMessage());
        }
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

        try {
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
            String statusNoticeKey = String.format("%s:NOTICE", event.eventId());
            ChatProposalStatusNoticePayloadResponse statusNoticePayload =
                    new ChatProposalStatusNoticePayloadResponse(
                            event.proposalId(),
                            event.actorUserId(),
                            LocalDateTime.now()
                    );

            Map<String, Object> noticeData = new HashMap<>();
            noticeData.put("proposalId", event.proposalId());
            if (event.actorUserId() != null) {
                noticeData.put("actorUserId", event.actorUserId());
            }

            retrySender.sendWithIdempotency(
                    statusNoticeKey,
                    roomId,
                    ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                    statusNoticePayload,
                    "ProposalStatusChangedEvent",
                    noticeData
            );

            // 4. 매칭 완료 시 추가 카드 전송 (별도 멱등성 키)
            if (event.newStatus() == ChatProposalStatus.MATCHED) {
                String matchedCardKey = String.format("%s:MATCHED_CARD", event.eventId());
                
                ChatMatchedCampaignPayloadResponse matchedPayload = matchedCampaignPayloadProvider.getPayload(event.campaignId())
                        .orElseThrow(() -> {
                            String message = String.format(
                                    "Failed to get matched campaign payload. campaignId=%d may not exist or be deleted",
                                    event.campaignId()
                            );
                            LOG.warn("[Proposal] {}. eventId={}, roomId={}", message, event.eventId(), roomId);
                            return new IllegalStateException(message);
                        });

                Map<String, Object> matchedData = new HashMap<>();
                matchedData.put("campaignId", event.campaignId());
                matchedData.put("proposalId", event.proposalId());
                matchedData.put("messageKind", "MATCHED_CAMPAIGN_CARD");

                retrySender.sendWithIdempotency(
                        matchedCardKey,
                        roomId,
                        ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                        matchedPayload,
                        "ProposalStatusChangedEvent",
                        matchedData
                );
            }

            LOG.info("[Proposal] Status change processed. roomId={}, status={}",
                    roomId, chatStatus);

        } catch (ChatRoomNotFoundException ex) {
            // 논리적 실패는 RetrySender에서 이미 처리됨
            LOG.warn("[Proposal] Logical failure (room not found). eventId={}", event.eventId());
        } catch (Exception ex) {
            LOG.error("[Proposal] Failed to handle status change. eventId={}, proposalId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(),
                    event.proposalId(),
                    event.brandUserId(),
                    event.creatorUserId(),
                    ex);
            // 전송 실패는 RetrySender의 @Recover에서 처리됨
        }
    }
}
