package com.example.RealMatch.chat.application.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.application.dto.response.ChatRoomCardResponse;
import com.example.RealMatch.chat.application.service.room.OpponentInfoService.OpponentInfo;
import com.example.RealMatch.chat.application.util.ChatConstants;
import com.example.RealMatch.chat.application.util.MessagePreviewGenerator;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomCardAssembler {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomCardAssembler.class);

    private final MessagePreviewGenerator messagePreviewGenerator;

    public List<ChatRoomCardResponse> assemble(
            List<ChatRoom> rooms,
            Long userId,
            Map<Long, ChatRoomMember> myMemberMap,
            Map<Long, Long> unreadCountMap,
            Map<Long, OpponentInfo> opponentInfoMap,
            Map<Long, ChatMessage> searchMatchByRoom,
            Set<Long> collaboratingRoomIds
    ) {
        return rooms.stream()
                .map(room -> assembleCard(room, userId, myMemberMap, unreadCountMap, opponentInfoMap, searchMatchByRoom, collaboratingRoomIds))
                .filter(Objects::nonNull)
                .toList();
    }

    private ChatRoomCardResponse assembleCard(
            ChatRoom room,
            Long userId,
            Map<Long, ChatRoomMember> myMemberMap,
            Map<Long, Long> unreadCountMap,
            Map<Long, OpponentInfo> opponentInfoMap,
            Map<Long, ChatMessage> searchMatchByRoom,
            Set<Long> collaboratingRoomIds
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

        boolean isCollaborating = collaboratingRoomIds.contains(room.getId());

        String preview;
        ChatMessageType messageType;
        LocalDateTime messageAt;

        if (searchMatchByRoom != null && searchMatchByRoom.containsKey(room.getId())) {
            ChatMessage matchMsg = searchMatchByRoom.get(room.getId());
            preview = messagePreviewGenerator.generate(matchMsg.getMessageType(), matchMsg.getContent());
            messageType = matchMsg.getMessageType();
            messageAt = matchMsg.getCreatedAt();
        } else {
            preview = room.getLastMessagePreview();
            messageType = room.getLastMessageType();
            messageAt = room.getLastMessageAt();
        }

        return new ChatRoomCardResponse(
                room.getId(),
                opponent.userId(),
                opponent.name(),
                opponent.profileImageUrl(),
                isCollaborating,
                preview,
                messageType,
                messageAt,
                unreadCountMap.getOrDefault(room.getId(), 0L)
        );
    }
}
