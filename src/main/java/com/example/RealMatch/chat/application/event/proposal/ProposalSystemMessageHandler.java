package com.example.RealMatch.chat.application.event.proposal;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalStatusNoticePayloadResponse;

import lombok.RequiredArgsConstructor;

/**
 * 제안(Proposal) 관련 시스템 메시지 처리를 담당하는 핸들러
 */
@Component
@RequiredArgsConstructor
public class ProposalSystemMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalSystemMessageHandler.class);

    private final ChatMessageSocketService chatMessageSocketService;
    private final ChatRoomUpdateService chatRoomUpdateService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final MatchedCampaignPayloadProvider matchedCampaignPayloadProvider;

    // 이벤트 중복 처리를 위한 간단한 인메모리 캐시
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private static final int MAX_PROCESSED_EVENTS = 10000;

    /**
     * 제안 전송 이벤트 처리
     * PROPOSAL_CARD 또는 RE_PROPOSAL_CARD 시스템 메시지를 전송합니다.
     */
    @Async
    public void handleProposalSent(ProposalSentEvent event) {
        if (!validateAndMarkProcessed(event.eventId(), "ProposalSentEvent")) {
            return;
        }

        try {
            if (event.roomId() == null || event.payload() == null) {
                LOG.warn("[Proposal] Invalid event. roomId={}, payload={}",
                        event.roomId(), event.payload());
                return;
            }

            LOG.info("[Proposal] Processing proposal sent. eventId={}, roomId={}, proposalId={}, isReProposal={}",
                    event.eventId(), event.roomId(), event.payload().proposalId(), event.isReProposal());

            ChatSystemMessageKind messageKind = event.isReProposal()
                    ? ChatSystemMessageKind.RE_PROPOSAL_CARD
                    : ChatSystemMessageKind.PROPOSAL_CARD;

            chatMessageSocketService.sendSystemMessage(
                    event.roomId(),
                    messageKind,
                    event.payload()
            );

            LOG.info("[Proposal] System message sent. roomId={}, proposalId={}, kind={}",
                    event.roomId(), event.payload().proposalId(), messageKind);

        } catch (Exception ex) {
            LOG.error("[Proposal] Failed to handle proposal sent. eventId={}, roomId={}, proposalId={}",
                    event.eventId(),
                    event.roomId(),
                    event.payload() != null ? event.payload().proposalId() : null,
                    ex);
        }
    }

    /**
     * 제안 상태 변경 이벤트 처리
     * PROPOSAL_STATUS_NOTICE 시스템 메시지를 전송하고, 매칭 시 MATCHED_CAMPAIGN_CARD도 전송합니다.
     */
    @Async
    public void handleProposalStatusChanged(ProposalStatusChangedEvent event) {
        if (!validateAndMarkProcessed(event.eventId(), "ProposalStatusChangedEvent")) {
            return;
        }

        try {
            LOG.info("[Proposal] Processing status change. eventId={}, proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            // 1. 채팅방 제안 상태 업데이트
            ChatProposalStatus chatStatus = event.newStatus();
            chatRoomUpdateService.updateProposalStatusByUsers(
                    event.brandUserId(),
                    event.creatorUserId(),
                    chatStatus
            );

            // 2. 채팅방 조회
            Optional<Long> roomIdOpt = chatRoomQueryService.getRoomIdByUserPair(
                    event.brandUserId(), event.creatorUserId());
            if (roomIdOpt.isEmpty()) {
                LOG.debug("[Proposal] Chat room not found. brandUserId={}, creatorUserId={}",
                        event.brandUserId(), event.creatorUserId());
                return;
            }
            Long roomId = roomIdOpt.get();

            // 3. 상태 변경 알림 메시지 전송
            ChatProposalStatusNoticePayloadResponse statusNoticePayload =
                    new ChatProposalStatusNoticePayloadResponse(
                            event.proposalId(),
                            event.actorUserId(),
                            LocalDateTime.now()
                    );
            chatMessageSocketService.sendSystemMessage(
                    roomId,
                    ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                    statusNoticePayload
            );

            // 4. 매칭 완료 시 추가 카드 전송
            if (event.newStatus() == ChatProposalStatus.MATCHED) {
                sendMatchedCampaignCard(roomId, event.campaignId());
            }

            LOG.info("[Proposal] Status change processed. roomId={}, status={}",
                    roomId, chatStatus);

        } catch (Exception ex) {
            LOG.error("[Proposal] Failed to handle status change. eventId={}, proposalId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(),
                    event.proposalId(),
                    event.brandUserId(),
                    event.creatorUserId(),
                    ex);
        }
    }

    private void sendMatchedCampaignCard(Long roomId, Long campaignId) {
        matchedCampaignPayloadProvider.getPayload(campaignId)
                .ifPresent(payload -> {
                    chatMessageSocketService.sendSystemMessage(
                            roomId,
                            ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                            payload
                    );
                    LOG.info("[Proposal] Matched campaign card sent. roomId={}, campaignId={}",
                            roomId, campaignId);
                });
    }

    /**
     * 이벤트 중복 처리 검증 및 마킹
     */
    private boolean validateAndMarkProcessed(String eventId, String eventType) {
        if (eventId == null) {
            LOG.warn("[Proposal] {} has null eventId, skipping duplicate check", eventType);
            return true;
        }

        // 캐시 크기 제한 (간단한 구현, 프로덕션에서는 TTL 기반 Redis 권장)
        if (processedEventIds.size() > MAX_PROCESSED_EVENTS) {
            processedEventIds.clear();
            LOG.info("[Proposal] Cleared processed event cache (size exceeded)");
        }

        if (!processedEventIds.add(eventId)) {
            LOG.warn("[Proposal] Duplicate {} detected, skipping. eventId={}", eventType, eventId);
            return false;
        }

        return true;
    }
}
