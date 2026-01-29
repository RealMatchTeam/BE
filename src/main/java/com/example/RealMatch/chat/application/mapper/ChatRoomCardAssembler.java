package com.example.RealMatch.chat.application.mapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.service.room.OpponentInfoService.OpponentInfo;
import com.example.RealMatch.chat.application.util.ChatConstants;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCardResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomCardAssembler {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomCardAssembler.class);

    public List<ChatRoomCardResponse> assemble(
            List<ChatRoom> rooms,
            Long userId,
            Map<Long, ChatRoomMember> myMemberMap,
            Map<Long, Long> unreadCountMap,
            Map<Long, OpponentInfo> opponentInfoMap
    ) {
        return rooms.stream()
                .map(room -> assembleCard(room, userId, myMemberMap, unreadCountMap, opponentInfoMap))
                .filter(Objects::nonNull)
                .toList();
    }

    private ChatRoomCardResponse assembleCard(
            ChatRoom room,
            Long userId,
            Map<Long, ChatRoomMember> myMemberMap,
            Map<Long, Long> unreadCountMap,
            Map<Long, OpponentInfo> opponentInfoMap
    ) {
        ChatRoomMember member = myMemberMap.get(room.getId());
        if (member == null) {
            LOG.warn("ChatRoomMember not found for roomId={}, userId={}. This may indicate data integrity issue.",
                    room.getId(), userId);
            return null;
        }

        OpponentInfo opponent = opponentInfoMap.getOrDefault(
                room.getId(),
                new OpponentInfo(null, ChatConstants.UNKNOWN_OPPONENT_NAME, null)
        );

        boolean isCollaborating = room.isCollaborating();

        return new ChatRoomCardResponse(
                room.getId(),
                opponent.userId(),
                opponent.name(),
                opponent.profileImageUrl(),
                isCollaborating,
                room.getLastMessagePreview(),
                room.getLastMessageType(),
                room.getLastMessageAt(),
                unreadCountMap.getOrDefault(room.getId(), 0L)
        );
    }
}
