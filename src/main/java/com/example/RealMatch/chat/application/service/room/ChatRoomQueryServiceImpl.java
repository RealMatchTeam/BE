package com.example.RealMatch.chat.application.service.room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.application.service.room.OpponentInfoService.OpponentInfo;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepositoryCustom.RoomCursorInfo;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.response.CampaignSummaryResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomQueryServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final OpponentInfoService opponentInfoService;
    private final CampaignSummaryService campaignSummaryService;

    @Override
    public ChatRoomListResponse getRoomList(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size
    ) {
        RoomCursorInfo cursorInfo = roomCursor != null
                ? new RoomCursorInfo(roomCursor.lastMessageAt(), roomCursor.roomId())
                : null;

        List<ChatRoom> rooms = chatRoomRepository.findRoomsByUser(
                userId, filterStatus, cursorInfo, size
        );

        boolean hasNext = rooms.size() > size;
        if (hasNext) {
            rooms = rooms.subList(0, size);
        }

        // 전체 미읽음 개수 계산
        long totalUnreadCount = chatRoomRepository.countTotalUnreadMessages(userId);

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
                .filter(m -> !m.isDeleted())
                .collect(Collectors.toMap(ChatRoomMember::getRoomId, m -> m));

        Map<Long, Long> unreadCountMap = chatRoomRepository.countUnreadMessagesByRoomIds(
                roomIds, userId
        );

        Map<Long, OpponentInfo> opponentInfoMap = opponentInfoService.getOpponentInfoMapBatch(userId, roomIds);

        List<ChatRoomCardResponse> roomCards = assembleRoomCards(
                rooms, userId, myMemberMap, unreadCountMap, opponentInfoMap
        );

        return new ChatRoomListResponse(
                totalUnreadCount,
                roomCards,
                nextCursor,
                hasNext
        );
    }

    @Override
    public ChatRoomDetailResponse getChatRoomDetailWithOpponent(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        // 내 멤버십 확인 (활성 상태)
        chatRoomMemberRepository.findActiveMemberByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> {
                    // 활성 멤버가 아닌 경우, 나간 멤버인지 확인
                    ChatRoomMember member = chatRoomMemberRepository
                            .findMemberByRoomIdAndUserIdWithRoomCheck(roomId, userId)
                            .orElse(null);
                    if (member != null && member.getLeftAt() != null) {
                        return new ChatException(ChatErrorCode.USER_LEFT_ROOM);
                    }
                    return new ChatException(ChatErrorCode.NOT_ROOM_MEMBER);
                });

        // 상대방 멤버 조회 (1:1 채팅방이므로 상대방은 1명)
        ChatRoomMember opponentMember = opponentInfoService.getOpponentMember(roomId, userId);
        OpponentInfo opponent = opponentInfoService.getOpponentInfo(opponentMember.getUserId());

        // 협업중 여부 판단
        boolean isCollaborating = room.isCollaborating();

        // 협업 요약 바 정보 조회 (제안이 있는 경우)
        CampaignSummaryResponse campaignSummary = campaignSummaryService.getCampaignSummary(roomId);

        return new ChatRoomDetailResponse(
                room.getId(),
                opponent.userId(),
                opponent.name(),
                opponent.profileImageUrl(),
                isCollaborating,
                campaignSummary
        );
    }


    private List<ChatRoomCardResponse> assembleRoomCards(
            List<ChatRoom> rooms,
            Long userId,
            Map<Long, ChatRoomMember> myMemberMap,
            Map<Long, Long> unreadCountMap,
            Map<Long, OpponentInfo> opponentInfoMap
    ) {
        List<ChatRoomCardResponse> roomCards = new ArrayList<>();
        for (ChatRoom room : rooms) {
            ChatRoomMember member = myMemberMap.get(room.getId());
            if (member == null) {
                LOG.warn("ChatRoomMember not found for roomId={}, userId={}. This may indicate data integrity issue.",
                        room.getId(), userId);
                continue;
            }
            OpponentInfo opponent = opponentInfoMap.getOrDefault(
                    room.getId(),
                    new OpponentInfo(null, "알 수 없음", null)
            );
            
            // 협업중 여부 판단
            boolean isCollaborating = room.isCollaborating();

            roomCards.add(new ChatRoomCardResponse(
                    room.getId(),
                    opponent.userId(),
                    opponent.name(),
                    opponent.profileImageUrl(),
                    isCollaborating,
                    room.getLastMessagePreview(),
                    room.getLastMessageType(),
                    room.getLastMessageAt(),
                    unreadCountMap.getOrDefault(room.getId(), 0L)
            ));
        }
        return roomCards;
    }


}
