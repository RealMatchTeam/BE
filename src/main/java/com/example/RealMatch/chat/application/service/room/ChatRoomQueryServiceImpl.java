package com.example.RealMatch.chat.application.service.room;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.application.mapper.ChatRoomCardAssembler;
import com.example.RealMatch.chat.application.service.room.OpponentInfoService.OpponentInfo;
import com.example.RealMatch.chat.application.util.ChatRoomKeyGenerator;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepositoryCustom.RoomCursorInfo;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.response.CampaignSummaryResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberService chatRoomMemberService;
    private final OpponentInfoService opponentInfoService;
    private final CampaignSummaryService campaignSummaryService;
    private final ChatRoomCardAssembler roomCardAssembler;

    @Override
    public Optional<Long> getRoomIdByUserPair(Long brandUserId, Long creatorUserId) {
        if (brandUserId == null || creatorUserId == null) {
            return Optional.empty();
        }
        String roomKey = ChatRoomKeyGenerator.createDirectRoomKey(brandUserId, creatorUserId);
        return chatRoomRepository.findByRoomKey(roomKey).map(ChatRoom::getId);
    }

    @Override
    public ChatRoomListResponse getRoomList(
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

        Map<Long, ChatMessage> searchMatchByRoom = null;
        if (StringUtils.hasText(search)) {
            searchMatchByRoom = chatMessageRepository.findLatestMatchingMessageByRoomIds(roomIds, search.trim());
        }

        List<ChatRoomCardResponse> roomCards = roomCardAssembler.assemble(
                rooms, userId, myMemberMap, unreadCountMap, opponentInfoMap, searchMatchByRoom
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
                .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

        // 내 멤버십 확인 (활성 상태)
        chatRoomMemberService.getActiveMemberOrThrow(roomId, userId);

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
}
