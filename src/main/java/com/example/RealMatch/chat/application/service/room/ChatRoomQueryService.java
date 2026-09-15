package com.example.RealMatch.chat.application.service.room;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.application.dto.response.CampaignSummaryResponse;
import com.example.RealMatch.chat.application.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.application.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.application.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.application.dto.response.ChatRoomListResponse;
import com.example.RealMatch.chat.application.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.application.mapper.ChatRoomCardAssembler;
import com.example.RealMatch.chat.application.repository.ChatMessageRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomRepositoryCustom.RoomCursorInfo;
import com.example.RealMatch.chat.application.service.room.OpponentInfoService.OpponentInfo;
import com.example.RealMatch.chat.application.util.ChatRoomKeyGenerator;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.global.common.QueryLimits;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class ChatRoomQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomQueryService.class);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberService chatRoomMemberService;
    private final OpponentInfoService opponentInfoService;
    private final CampaignSummaryService campaignSummaryService;
    private final ChatRoomCardAssembler roomCardAssembler;
    private final SystemMessagePayloadSerializer payloadSerializer;

    public Optional<Long> getRoomIdByUserPair(Long brandUserId, Long creatorUserId) {
        if (brandUserId == null || creatorUserId == null) {
            return Optional.empty();
        }
        String roomKey = ChatRoomKeyGenerator.createDirectRoomKey(brandUserId, creatorUserId);
        return chatRoomRepository.findByRoomKey(roomKey).map(ChatRoom::getId);
    }

    public ChatRoomListResponse getRoomList(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search
    ) {
        QueryLimits.page(0, size);
        QueryLimits.text(search, 200);
        return loadChatRoomList(userId, filterStatus, roomCursor, size, search);
    }

    private ChatRoomListResponse loadChatRoomList(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size,
            String search
    ) {
        RoomCursorInfo cursorInfo = roomCursor != null
                ? new RoomCursorInfo(roomCursor.lastMessageAt(), roomCursor.roomId())
                : null;

        List<ChatRoom> rooms = chatRoomRepository.findRoomsByUser(
                userId, filterStatus, cursorInfo, size, search
        );

        boolean hasNext = rooms.size() > size;
        if (hasNext) {
            rooms = rooms.subList(0, size);
        }

        // Total and room-card unread counts use the same aggregate snapshot.
        Map<Long, Long> unreadCountMap = chatRoomRepository.countUnreadMessagesByUser(userId);
        long totalUnreadCount = unreadCountMap.values().stream().mapToLong(Long::longValue).sum();

        if (rooms.isEmpty()) {
            return new ChatRoomListResponse(
                    totalUnreadCount,
                    List.of(), null, false
            );
        }

        RoomCursor nextCursor = null;
        if (hasNext) {
            ChatRoom lastRoom = rooms.getLast();
            nextCursor = RoomCursor.of(lastRoom.getLastMessageAt(), lastRoom.getId());
        }

        List<Long> roomIds = rooms.stream().map(ChatRoom::getId).toList();

        Map<Long, ChatRoomMember> myMemberMap = chatRoomMemberRepository
                .findByUserIdAndRoomIdIn(userId, roomIds).stream()
                .filter(m -> !m.isDeleted() && m.getLeftAt() == null)
                .collect(Collectors.toMap(ChatRoomMember::getRoomId, m -> m));

        Map<Long, OpponentInfo> opponentInfoMap = opponentInfoService.getOpponentInfoMapBatch(userId, roomIds);

        Map<Long, ChatMessage> searchMatchByRoom = null;
        if (StringUtils.hasText(search)) {
            searchMatchByRoom = chatMessageRepository.findLatestMatchingMessageByRoomIds(roomIds, search.trim());
        }

        List<ChatRoomCardResponse> roomCards = roomCardAssembler.assemble(
                rooms, userId, myMemberMap, unreadCountMap, opponentInfoMap, searchMatchByRoom, chatRoomRepository.findCollaboratingRoomIds(roomIds)
        );

        return new ChatRoomListResponse(
                totalUnreadCount,
                roomCards,
                nextCursor,
                hasNext
        );
    }

    public ChatRoomDetailResponse getChatRoomDetailWithOpponent(Long userId, Long roomId) {
        ChatRoomMember member = chatRoomMemberService.getActiveMemberOrThrow(roomId, userId);
        return loadChatRoomDetail(userId, roomId, member.getRole());
    }

    private ChatRoomDetailResponse loadChatRoomDetail(Long userId, Long roomId, ChatRoomMemberRole myRole) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

        // 상대방 멤버 조회 (1:1 채팅방이므로 상대방은 1명)
        OpponentInfo opponent = opponentInfoService.getOpponentInfoMapBatch(userId, List.of(roomId)).get(roomId);

        // 협업중 여부 판단
        boolean isCollaborating = chatRoomRepository.findCollaboratingRoomIds(List.of(roomId)).contains(roomId);

        // 협업 요약 바: 협업중일 때만 노출
        CampaignSummaryResponse campaignSummary = isCollaborating
                ? campaignSummaryService.getCampaignSummary(roomId)
                : null;

        // 재제안 폼 진입용: 최신 PROPOSAL_CARD/RE_PROPOSAL_CARD 메시지에서 proposalId 추출
        Long latestProposalId = resolveLatestProposalId(roomId);

        return new ChatRoomDetailResponse(
                room.getId(),
                opponent.userId(),
                opponent.name(),
                opponent.profileImageUrl(),
                isCollaborating,
                campaignSummary,
                latestProposalId,
                myRole
        );
    }

    private Long resolveLatestProposalId(Long roomId) {
        return chatMessageRepository.findLatestProposalCardMessageByRoomId(roomId)
                .flatMap(msg -> {
                    String rawPayload = msg.getSystemPayload();
                    ChatSystemMessageKind kind = msg.getSystemKind();
                    if (rawPayload == null || rawPayload.isBlank() || kind == null) {
                        return Optional.<Long>empty();
                    }
                    try {
                        ChatSystemMessagePayload payload = payloadSerializer.deserialize(kind, rawPayload);
                        if (payload instanceof ChatProposalCardPayloadResponse card) {
                            return Optional.ofNullable(card.proposalId());
                        }
                    } catch (Exception ex) {
                        LOG.warn("Failed to deserialize system message payload for roomId={}, kind={}, payloadLength={}",
                                roomId, kind, rawPayload.length(), ex);
                    }
                    return Optional.<Long>empty();
                })
                .orElse(null);
    }
}
