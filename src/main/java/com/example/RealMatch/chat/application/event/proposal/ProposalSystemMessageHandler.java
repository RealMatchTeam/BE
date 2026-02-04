package com.example.RealMatch.chat.application.event.proposal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.idempotency.FailedEventDlq;
import com.example.RealMatch.chat.application.idempotency.ProcessedEventStore;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryService;
import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.application.service.room.MatchedCampaignPayloadProvider;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;
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
    private final ProcessedEventStore processedEventStore;
    private final FailedEventDlq failedEventDlq;

    private static final Duration EVENT_IDEMPOTENCY_TTL = Duration.ofHours(6);

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

        // 2. Redis 멱등성 체크 (중복 방지용, 전송 전에만 체크)
        if (!checkIfNotProcessed(event.eventId(), "ProposalSentEvent")) {
            return;
        }

        try {
            LOG.info("[Proposal] Processing proposal sent. eventId={}, roomId={}, proposalId={}, isReProposal={}",
                    event.eventId(), event.roomId(), event.payload().proposalId(), event.isReProposal());

            ChatSystemMessageKind messageKind = event.isReProposal()
                    ? ChatSystemMessageKind.RE_PROPOSAL_CARD
                    : ChatSystemMessageKind.PROPOSAL_CARD;

            // 3. 전송 시도 (성공 시 Redis 키가 남음)
            sendProposalCard(event.roomId(), messageKind, event.payload(), event.eventId());

            LOG.info("[Proposal] System message sent. roomId={}, proposalId={}, kind={}",
                    event.roomId(), event.payload().proposalId(), messageKind);

        } catch (IllegalArgumentException ex) {
            // 논리적 실패: Redis 키 삭제
            LOG.warn("[Proposal] Logical failure. Removing Redis key. eventId={}, error={}",
                    event.eventId(), ex.getMessage());
            processedEventStore.removeProcessed(event.eventId());
        } catch (Exception ex) {
            LOG.error("[Proposal] Failed to handle proposal sent. eventId={}, roomId={}, proposalId={}",
                    event.eventId(),
                    event.roomId(),
                    event.payload() != null ? event.payload().proposalId() : null,
                    ex);
            // 전송 실패 시 Redis 키는 유지됨 (재시도 방지)
            // 최종 실패 시 @Recover에서 키를 삭제하여 재처리 가능하도록 함
        }
    }

    /**
     * 제안 상태 변경 이벤트 처리
     * PROPOSAL_STATUS_NOTICE 시스템 메시지를 전송하고, 매칭 시 MATCHED_CAMPAIGN_CARD도 전송합니다.
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

        // 2. Redis 멱등성 체크 (중복 방지용, 전송 전에만 체크)
        if (!checkIfNotProcessed(event.eventId(), "ProposalStatusChangedEvent")) {
            return;
        }

        try {
            LOG.info("[Proposal] Processing status change. eventId={}, proposalId={}, newStatus={}, brandUserId={}, creatorUserId={}",
                    event.eventId(), event.proposalId(), event.newStatus(), event.brandUserId(), event.creatorUserId());

            // 3. 채팅방 제안 상태 업데이트
            ChatProposalStatus chatStatus = event.newStatus();
            chatRoomUpdateService.updateProposalStatusByUsers(
                    event.brandUserId(),
                    event.creatorUserId(),
                    chatStatus
            );

            // 4. 상태 변경 알림 메시지 전송 (재시도 가능, 성공 시 Redis 키 유지)
            sendProposalStatusNotice(roomId, event.proposalId(), event.actorUserId(), event.eventId());

            // 5. 매칭 완료 시 추가 카드 전송 (재시도 가능)
            if (event.newStatus() == ChatProposalStatus.MATCHED) {
                sendMatchedCampaignCard(roomId, event.campaignId(), event.eventId());
            }

            LOG.info("[Proposal] Status change processed. roomId={}, status={}",
                    roomId, chatStatus);

        } catch (ChatRoomNotFoundException ex) {
            // 논리적 실패: Redis 키 삭제
            LOG.warn("[Proposal] Logical failure (room not found). Removing Redis key. eventId={}",
                    event.eventId());
            processedEventStore.removeProcessed(event.eventId());
        } catch (Exception ex) {
            LOG.error("[Proposal] Failed to handle status change. eventId={}, proposalId={}, brandUserId={}, creatorUserId={}",
                    event.eventId(),
                    event.proposalId(),
                    event.brandUserId(),
                    event.creatorUserId(),
                    ex);
            // 전송 실패 시 Redis 키는 유지됨 (재시도 방지)
            // 최종 실패는 @Recover에서 처리
        }
    }

    /**
     * 제안 상태 변경 알림 메시지 전송 (재시도 가능)
     * 전송 성공 시 Redis 키를 확실히 남깁니다.
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {ChatRoomNotFoundException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    private void sendProposalStatusNotice(Long roomId, Long proposalId, Long actorUserId, String eventId) {
        try {
            LOG.info("[Proposal] Attempting to send proposal status notice. eventId={}, roomId={}, proposalId={}",
                    eventId, roomId, proposalId);
            ChatProposalStatusNoticePayloadResponse statusNoticePayload =
                    new ChatProposalStatusNoticePayloadResponse(
                            proposalId,
                            actorUserId,
                            LocalDateTime.now()
                    );
            chatMessageSocketService.sendSystemMessage(
                    roomId,
                    ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                    statusNoticePayload
            );
            // 전송 성공 후 Redis 키를 확실히 남김
            processedEventStore.markAsProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
            LOG.info("[Proposal] Proposal status notice sent successfully. eventId={}, roomId={}", eventId, roomId);
        } catch (Exception ex) {
            // 재시도 가능한 예외는 Spring Retry가 처리
            // 최종 실패는 @Recover에서 키 삭제
            LOG.warn("[Proposal] Failed to send proposal status notice (will retry). eventId={}, roomId={}, error={}",
                    eventId, roomId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 매칭 완료 카드 전송 (재시도 가능)
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {ChatRoomNotFoundException.class, IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    private void sendMatchedCampaignCard(Long roomId, Long campaignId, String eventId) {
        try {
            LOG.info("[Proposal] Attempting to send matched campaign card. eventId={}, roomId={}, campaignId={}",
                    eventId, roomId, campaignId);
            matchedCampaignPayloadProvider.getPayload(campaignId)
                    .ifPresent(payload -> {
                        chatMessageSocketService.sendSystemMessage(
                                roomId,
                                ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD,
                                payload
                        );
                        LOG.info("[Proposal] Matched campaign card sent successfully. eventId={}, roomId={}, campaignId={}",
                                eventId, roomId, campaignId);
                    });
        } catch (Exception ex) {
            // 재시도 가능한 예외는 Spring Retry가 처리
            // 최종 실패는 @Recover에서 키 삭제
            LOG.warn("[Proposal] Failed to send matched campaign card (will retry). eventId={}, roomId={}, campaignId={}, error={}",
                    eventId, roomId, campaignId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 제안 상태 변경 알림 메시지 전송 실패 시 복구 처리
     */
    @Recover
    private void recoverProposalStatusNotice(Exception ex, Long roomId, Long proposalId, Long actorUserId, String eventId) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(eventId);
        LOG.error("[Proposal] Failed to send proposal status notice after all retries (3 attempts). " +
                        "eventId={}, roomId={}, proposalId={}, actorUserId={}, Redis key removed",
                eventId, roomId, proposalId, actorUserId, ex);
        
        Map<String, Object> additionalData = new HashMap<>();
        if (proposalId != null) {
            additionalData.put("proposalId", proposalId);
        }
        if (actorUserId != null) {
            additionalData.put("actorUserId", actorUserId);
        }
        failedEventDlq.enqueueFailedEvent(
                "ProposalStatusChangedEvent",
                eventId,
                roomId,
                ex.getMessage(),
                additionalData
        );
    }

    /**
     * 매칭 완료 카드 전송 실패 시 복구 처리
     */
    @Recover
    private void recoverMatchedCampaignCard(Exception ex, Long roomId, Long campaignId, String eventId) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(eventId);
        LOG.error("[Proposal] Failed to send matched campaign card after all retries (3 attempts). " +
                        "eventId={}, roomId={}, campaignId={}, Redis key removed",
                eventId, roomId, campaignId, ex);
        
        Map<String, Object> additionalData = new HashMap<>();
        if (campaignId != null) {
            additionalData.put("campaignId", campaignId);
        }
        additionalData.put("messageKind", "MATCHED_CAMPAIGN_CARD");
        failedEventDlq.enqueueFailedEvent(
                "ProposalStatusChangedEvent",
                eventId,
                roomId,
                ex.getMessage(),
                additionalData
        );
    }

    /**
     * 제안 카드 전송 (재시도 가능)
     * 전송 성공 시 Redis 키를 확실히 남깁니다.
     */
    @Retryable(
            retryFor = {Exception.class},
            noRetryFor = {IllegalArgumentException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2, maxDelay = 800)
    )
    private void sendProposalCard(Long roomId, ChatSystemMessageKind messageKind, ChatProposalCardPayloadResponse payload, String eventId) {
        try {
            LOG.info("[Proposal] Attempting to send proposal card. eventId={}, roomId={}, kind={}, proposalId={}",
                    eventId, roomId, messageKind, payload != null ? payload.proposalId() : null);
            chatMessageSocketService.sendSystemMessage(roomId, messageKind, payload);
            // 전송 성공 후 Redis 키를 확실히 남김
            processedEventStore.markAsProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
            LOG.info("[Proposal] Proposal card sent successfully. eventId={}, roomId={}, kind={}", eventId, roomId, messageKind);
        } catch (Exception ex) {
            // 재시도 가능한 예외는 Spring Retry가 처리
            // 최종 실패는 @Recover에서 키 삭제
            LOG.warn("[Proposal] Failed to send proposal card (will retry). eventId={}, roomId={}, kind={}, error={}",
                    eventId, roomId, messageKind, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 제안 카드 전송 실패 시 복구 처리
     */
    @Recover
    private void recoverProposalCard(Exception ex, Long roomId, ChatSystemMessageKind messageKind, ChatProposalCardPayloadResponse payload, String eventId) {
        // 전송 실패 시 Redis 키 삭제 (재시도 가능하도록)
        processedEventStore.removeProcessed(eventId);
        LOG.error("[Proposal] Failed to send proposal card after all retries (3 attempts). " +
                        "eventId={}, roomId={}, kind={}, proposalId={}, Redis key removed",
                eventId, roomId, messageKind, payload != null ? payload.proposalId() : null, ex);
        
        Map<String, Object> additionalData = new HashMap<>();
        if (payload != null && payload.proposalId() != null) {
            additionalData.put("proposalId", payload.proposalId());
        }
        additionalData.put("messageKind", messageKind != null ? messageKind.toString() : "UNKNOWN");
        failedEventDlq.enqueueFailedEvent(
                "ProposalSentEvent",
                eventId,
                roomId,
                ex.getMessage(),
                additionalData
        );
    }

    /**
     * 이벤트 중복 처리 검증 (Redis 기반)
     * 
     * <p>처리 흐름:
     * <ol>
     *   <li>markIfNotProcessed(): SETNX로 처리 시작 시점에 키 생성 (중복 체크)</li>
     *   <li>전송 성공 시: markAsProcessed()로 키를 확실히 남김 (SET 명령어)</li>
     *   <li>논리적 실패 시: removeProcessed()로 키 삭제</li>
     *   <li>최종 실패 시: @Recover에서 removeProcessed()로 키 삭제</li>
     * </ol>
     */
    private boolean checkIfNotProcessed(String eventId, String eventType) {
        if (eventId == null) {
            LOG.error("[Proposal] {} has null eventId, this should never happen. Rejecting event.", eventType);
            throw new IllegalStateException("Event ID cannot be null for " + eventType);
        }

        // SETNX로 처리 시작 시점에 키 생성 (중복 체크)
        boolean isNewEvent = processedEventStore.markIfNotProcessed(eventId, EVENT_IDEMPOTENCY_TTL);
        if (!isNewEvent) {
            LOG.warn("[Proposal] Duplicate {} detected, skipping. eventId={}", eventType, eventId);
            return false;
        }

        return true;
    }
}
