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
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.repository.CampaignRepository;
import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepositoryCustom.RoomCursorInfo;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.response.CampaignSummaryResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMatchedCampaignPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatProposalCardPayloadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
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
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CampaignRepository campaignRepository;
    private final SystemMessagePayloadSerializer payloadSerializer;

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

        Map<Long, OpponentInfo> opponentInfoMap = getOpponentInfoMapBatch(userId, roomIds);

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

        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByRoomId(roomId);
        allMembers.stream()
                .filter(m -> m.getUserId().equals(userId) && !m.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_ROOM_MEMBER));

        ChatRoomMember opponentMember = allMembers.stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ChatException(ChatErrorCode.INTERNAL_ERROR, "Opponent member not found"));

        OpponentInfo opponent = getOpponentInfo(opponentMember.getUserId());

        // 협업중 여부 판단 (proposalStatus == MATCHED)
        boolean isCollaborating = room.getProposalStatus() == ChatProposalStatus.MATCHED;

        // 협업 요약 바 정보 조회 (제안이 있는 경우)
        CampaignSummaryResponse campaignSummary = getCampaignSummary(roomId);

        return new ChatRoomDetailResponse(
                room.getId(),
                opponent.userId(),
                opponent.name(),
                opponent.profileImageUrl(),
                isCollaborating,
                campaignSummary
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
            
            // 협업중 여부 판단 (proposalStatus == MATCHED)
            boolean isCollaborating = room.getProposalStatus() == ChatProposalStatus.MATCHED;

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


    private CampaignSummaryResponse getCampaignSummary(Long roomId) {
        List<ChatMessage> proposalMessages = chatMessageRepository.findProposalMessagesByRoomId(roomId);

        if (proposalMessages.isEmpty()) {
            return null; // 제안이 없는 경우
        }

        // 가장 최근 제안 메시지에서 campaignId 추출
        ChatMessage latestProposalMessage = proposalMessages.get(0);
        ChatSystemMessageKind kind = latestProposalMessage.getSystemKind();
        String rawPayload = latestProposalMessage.getSystemPayload();

        if (rawPayload == null || rawPayload.isBlank()) {
            return null;
        }

        try {
            ChatSystemMessagePayload payload = payloadSerializer.deserialize(kind, rawPayload);
            Long campaignId = null;

            if (payload instanceof ChatProposalCardPayloadResponse proposalCard) {
                campaignId = proposalCard.campaignId();
            } else if (payload instanceof ChatMatchedCampaignPayloadResponse matchedCard) {
                campaignId = matchedCard.campaignId();
            }

            if (campaignId == null) {
                return null;
            }

            // 캠페인 정보 조회
            Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
            if (campaign == null || campaign.isDeleted()) {
                return null;
            }

            // 브랜드 정보 조회
            Brand brand = brandRepository.findByCreatedBy(campaign.getCreatedBy()).orElse(null);
            String brandName = brand != null && !brand.isDeleted() ? brand.getBrandName() : null;

            return new CampaignSummaryResponse(
                    campaignId,
                    null, // 캠페인 대표 이미지는 현재 Campaign 엔티티에 없음
                    brandName,
                    campaign.getTitle()
            );
        } catch (Exception ex) {
            LOG.warn("Failed to extract campaign summary from system message. roomId={}, messageId={}", 
                    roomId, latestProposalMessage.getId(), ex);
            return null;
        }
    }

    private record OpponentInfo(Long userId, String name, String profileImageUrl) {
    }
}
