package com.example.RealMatch.chat.application.service.room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepositoryCustom.RoomCursorInfo;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;

    @Override
    public ChatRoomListResponse getRoomList(
            CustomUserDetails user,
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            RoomCursor roomCursor,
            int size
    ) {
        Long userId = user.getUserId();
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

        if (rooms.isEmpty()) {
            return new ChatRoomListResponse(
                    0L, 0L, 0L, List.of(), null, false
            );
        }

        RoomCursor nextCursor = null;
        if (hasNext && !rooms.isEmpty()) {
            ChatRoom lastRoom = rooms.get(rooms.size() - 1);
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

        Map<Long, OpponentInfo> opponentInfoMap = getOpponentInfoMapBatch(rooms, userId, roomIds);

        List<ChatRoomCardResponse> roomCards = new ArrayList<>();
        for (ChatRoom room : rooms) {
            ChatRoomMember member = myMemberMap.get(room.getId());
            if (member == null) {
                continue;
            }
            OpponentInfo opponent = opponentInfoMap.get(room.getId());
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
                    unreadCountMap.getOrDefault(room.getId(), 0L).intValue(),
                    tabCategory
            ));
        }

        long sentTabUnreadCount = chatRoomRepository.countUnreadMessagesByUserAndTab(userId, ChatRoomTab.SENT);
        long receivedTabUnreadCount = chatRoomRepository.countUnreadMessagesByUserAndTab(userId, ChatRoomTab.RECEIVED);
        long totalUnreadCount = sentTabUnreadCount + receivedTabUnreadCount;

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
    public ChatRoomDetailResponse getRoomDetail(CustomUserDetails user, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        Long userId = user.getUserId();
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of room: " + roomId));

        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByRoomId(roomId);
        ChatRoomMember opponentMember = allMembers.stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Opponent member not found"));

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

    private Map<Long, OpponentInfo> getOpponentInfoMapBatch(
            List<ChatRoom> rooms,
            Long userId,
            List<Long> roomIds
    ) {
        List<ChatRoomMember> opponentMembers = chatRoomMemberRepository
                .findByRoomIdIn(roomIds).stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .toList();

        Map<Long, Long> roomToOpponentUserIdMap = opponentMembers.stream()
                .collect(Collectors.toMap(
                        ChatRoomMember::getRoomId,
                        ChatRoomMember::getUserId,
                        (existing, replacement) -> existing
                ));

        Set<Long> opponentUserIds = new HashSet<>(roomToOpponentUserIdMap.values());
        if (opponentUserIds.isEmpty()) {
            return rooms.stream()
                    .collect(Collectors.toMap(
                            ChatRoom::getId,
                            room -> new OpponentInfo(null, "알 수 없음", null)
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

        return rooms.stream()
                .collect(Collectors.toMap(
                        ChatRoom::getId,
                        room -> {
                            Long opponentUserId = roomToOpponentUserIdMap.get(room.getId());
                            if (opponentUserId == null) {
                                return new OpponentInfo(null, "알 수 없음", null);
                            }
                            User user = userMap.get(opponentUserId);
                            if (user == null) {
                                return new OpponentInfo(opponentUserId, "알 수 없음", null);
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
            return new OpponentInfo(null, "알 수 없음", null);
        }

        User user = userRepository.findById(opponentUserId).orElse(null);
        if (user == null) {
            return new OpponentInfo(opponentUserId, "알 수 없음", null);
        }

        if (user.getRole() == Role.BRAND) {
            Brand brand = brandRepository.findByCreatedBy(opponentUserId).orElse(null);
            if (brand != null && !brand.isDeleted()) {
                return new OpponentInfo(opponentUserId, brand.getBrandName(), brand.getLogoUrl());
            }
        }

        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
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
        return ChatRoomTab.ALL;
    }

    private record OpponentInfo(Long userId, String name, String profileImageUrl) {
    }
}
