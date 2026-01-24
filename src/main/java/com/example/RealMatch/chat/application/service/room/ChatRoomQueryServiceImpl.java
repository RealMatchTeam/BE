package com.example.RealMatch.chat.application.service.room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepositoryCustom.RoomCursorInfo;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomQueryServiceImpl.class);
    private static final String UNKNOWN_OPPONENT_NAME = "알 수 없음";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;

    @Override
    public ChatRoomListResponse getRoomList(
            Long userId,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size
    ) {
        RoomCursorInfo cursorInfo = roomCursor != null
                ? new RoomCursorInfo(roomCursor.lastMessageAt(), roomCursor.roomId())
                : null;

        List<ChatRoom> rooms = chatRoomRepository.findRoomsByUser(
                userId, tab, filterStatus, cursorInfo, size
        );

        boolean hasNext = rooms.size() > size;
        if (hasNext) {
            rooms = rooms.subList(0, size);
        }

        // 탭 배지는 매칭 필터와 상관없이 전체 보낸 제안/받은 제안의 미읽음 총합을 계산
        // 예를 들어, 매칭 필터가 '매칭'일 때도 보낸 제안 탭 배지는 전체 보낸 제안의 미읽음 총합을 계산
        // 그리고 보낸 제안 / 받은 제안 탭이 각각 따로 계산되어야 함!! 
        // 예를 들어, 아직 읽지 않은 메시지가 총 9개라면 탭 옆 숫자 뱃지에 보낸 제안 5 / 받은 제안 4 이렇게 떠야 하는거임.
        Map<ChatRoomTab, Long> unreadCountByTab = chatRoomRepository.countUnreadMessagesByTabs(userId);
        long sentTabUnreadCount = unreadCountByTab.getOrDefault(ChatRoomTab.SENT, 0L);
        long receivedTabUnreadCount = unreadCountByTab.getOrDefault(ChatRoomTab.RECEIVED, 0L);
        long totalUnreadCount = sentTabUnreadCount + receivedTabUnreadCount;

        if (rooms.isEmpty()) {
            return new ChatRoomListResponse(
                    sentTabUnreadCount,
                    receivedTabUnreadCount,
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

        Map<Long, OpponentInfo> opponentInfoMap = getOpponentInfoMapBatch(userId, roomIds);

        List<ChatRoomCardResponse> roomCards = assembleRoomCards(
                rooms, userId, myMemberMap, unreadCountMap, opponentInfoMap
        );

        return new ChatRoomListResponse(
                sentTabUnreadCount,
                receivedTabUnreadCount,
                totalUnreadCount,
                roomCards,
                nextCursor,
                hasNext
        );
    }

    @Override
    public ChatRoomDetailResponse getRoomDetail(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_ROOM_MEMBER));

        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByRoomId(roomId);
        ChatRoomMember opponentMember = allMembers.stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ChatException(ChatErrorCode.INTERNAL_ERROR, "Opponent member not found"));

        OpponentInfo opponent = getOpponentInfo(opponentMember.getUserId());

        return new ChatRoomDetailResponse(
                room.getId(),
                opponent.userId(),
                opponent.name(),
                opponent.profileImageUrl(),
                List.of(),
                room.getProposalStatus(),
                room.getProposalStatus() != null ? room.getProposalStatus().label() : null
        );
    }

    // 1:1 채팅방에서의 상대방 정보 조회
    private Map<Long, OpponentInfo> getOpponentInfoMapBatch(
            Long userId,
            List<Long> roomIds
    ) {
        List<ChatRoomMember> opponentMembers = chatRoomMemberRepository
                .findByRoomIdIn(roomIds).stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .toList();

        Map<Long, List<ChatRoomMember>> opponentByRoom = opponentMembers.stream()
                .collect(Collectors.groupingBy(ChatRoomMember::getRoomId));

        for (Long roomId : roomIds) {
            List<ChatRoomMember> members = opponentByRoom.get(roomId);
            if (members == null || members.isEmpty()) {
                throw new ChatException(
                        ChatErrorCode.INTERNAL_ERROR,
                        "Chat room opponent not found (data integrity issue). roomId=" + roomId
                );
            }
            if (members.size() != 1) {
                throw new ChatException(
                        ChatErrorCode.INTERNAL_ERROR,
                        "Chat room is not 1:1. roomId=" + roomId + ", opponentCount=" + members.size()
                );
            }
        }

        Map<Long, Long> roomToOpponentUserIdMap = roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> opponentByRoom.get(roomId).getFirst().getUserId()
                ));

        Set<Long> opponentUserIds = new HashSet<>(roomToOpponentUserIdMap.values());
        if (opponentUserIds.isEmpty()) {
            return roomIds.stream()
                    .collect(Collectors.toMap(
                            roomId -> roomId,
                            roomId -> new OpponentInfo(null, UNKNOWN_OPPONENT_NAME, null)
                    ));
        }

        Map<Long, User> userMap = userRepository.findAllById(opponentUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> brandUserIds = userMap.values().stream()
                .filter(u -> u.getRole() == Role.BRAND)
                .map(User::getId)
                .toList();

        Map<Long, Brand> brandMap = brandUserIds.isEmpty()
                ? Map.of()
                : brandRepository.findByCreatedByIn(brandUserIds).stream()
                        .filter(b -> !b.isDeleted())
                        .collect(Collectors.toMap(Brand::getCreatedBy, b -> b));

        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> {
                            Long opponentUserId = roomToOpponentUserIdMap.get(roomId);
                            if (opponentUserId == null) {
                                return new OpponentInfo(null, UNKNOWN_OPPONENT_NAME, null);
                            }
                            User user = userMap.get(opponentUserId);
                            if (user == null) {
                                return new OpponentInfo(opponentUserId, UNKNOWN_OPPONENT_NAME, null);
                            }

                            if (user.getRole() == Role.BRAND) {
                                Brand brand = brandMap.get(opponentUserId);
                                if (brand != null) {
                                    return new OpponentInfo(opponentUserId, brand.getBrandName(), brand.getLogoUrl());
                                }
                            }

                            return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
                        }
                ));
    }

    private OpponentInfo getOpponentInfo(Long opponentUserId) {
        if (opponentUserId == null) {
            return new OpponentInfo(null, UNKNOWN_OPPONENT_NAME, null);
        }

        User user = userRepository.findById(opponentUserId).orElse(null);
        if (user == null) {
            return new OpponentInfo(opponentUserId, UNKNOWN_OPPONENT_NAME, null);
        }

        if (user.getRole() == Role.BRAND) {
            Brand brand = brandRepository.findByCreatedBy(opponentUserId).orElse(null);
            if (brand != null && !brand.isDeleted()) {
                return new OpponentInfo(opponentUserId, brand.getBrandName(), brand.getLogoUrl());
            }
        }

        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
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
                    new OpponentInfo(null, UNKNOWN_OPPONENT_NAME, null)
            );
            ChatRoomTab tabCategory = calculateTabCategory(room, member);

            roomCards.add(new ChatRoomCardResponse(
                    room.getId(),
                    opponent.userId(),
                    opponent.name(),
                    opponent.profileImageUrl(),
                    room.getProposalStatus(),
                    room.getLastMessagePreview(),
                    room.getLastMessageType(),
                    room.getLastMessageAt(),
                    unreadCountMap.getOrDefault(room.getId(), 0L),
                    tabCategory
            ));
        }
        return roomCards;
    }

    private ChatRoomTab calculateTabCategory(ChatRoom room, ChatRoomMember member) {
        ChatProposalDirection direction = room.getLastProposalDirection();
        ChatRoomMemberRole role = member.getRole();

        if (direction == ChatProposalDirection.BRAND_TO_CREATOR) {
            return role == ChatRoomMemberRole.BRAND ? ChatRoomTab.SENT : ChatRoomTab.RECEIVED;
        }
        if (direction == ChatProposalDirection.CREATOR_TO_BRAND) {
            return role == ChatRoomMemberRole.CREATOR ? ChatRoomTab.SENT : ChatRoomTab.RECEIVED;
        }
        // BRAND_TO_CREATOR, CREATOR_TO_BRAND가 아닌 경우 (NONE 또는 null)
        // 이론상 발생하지 않아야 함 (방 생성 시 BRAND_TO_CREATOR 또는 CREATOR_TO_BRAND로 초기화)
        // 하지만 방어적으로 예외 처리
        throw new ChatException(
                ChatErrorCode.INTERNAL_ERROR,
                String.format(
                        "ChatRoom with invalid direction should not appear in list. roomId=%d, direction=%s",
                        room.getId(), direction
                )
        );
    }

    private record OpponentInfo(Long userId, String name, String profileImageUrl) {
    }
}
