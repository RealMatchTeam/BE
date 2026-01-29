package com.example.RealMatch.chat.application.service.room;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.util.ChatConstants;
import com.example.RealMatch.chat.application.util.ChatRoomValidator;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpponentInfoServiceImpl implements OpponentInfoService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    @Override
    public OpponentInfo getOpponentInfo(Long opponentUserId) {
        if (opponentUserId == null) {
            return new OpponentInfo(null, ChatConstants.UNKNOWN_OPPONENT_NAME, null);
        }

        User user = userRepository.findById(opponentUserId).orElse(null);
        if (user == null) {
            return new OpponentInfo(opponentUserId, ChatConstants.UNKNOWN_OPPONENT_NAME, null);
        }

        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
    }

    @Override
    public Map<Long, OpponentInfo> getOpponentInfoMapBatch(Long userId, List<Long> roomIds) {
        List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findActiveMembersByRoomIdIn(roomIds);
        List<ChatRoomMember> opponentMembers = activeMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .toList();

        Map<Long, List<ChatRoomMember>> opponentByRoom = opponentMembers.stream()
                .collect(Collectors.groupingBy(ChatRoomMember::getRoomId));

        // 1:1 채팅방 검증
        for (Long roomId : roomIds) {
            List<ChatRoomMember> members = opponentByRoom.get(roomId);
            ChatRoomValidator.validateDirectRoomOpponent(members, roomId);
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
                            roomId -> new OpponentInfo(null, ChatConstants.UNKNOWN_OPPONENT_NAME, null)
                    ));
        }

        Map<Long, User> userMap = userRepository.findAllById(opponentUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> {
                            Long opponentUserId = roomToOpponentUserIdMap.get(roomId);
                            User user = userMap.get(opponentUserId);
                            if (user == null) {
                                return new OpponentInfo(opponentUserId, ChatConstants.UNKNOWN_OPPONENT_NAME, null);
                            }

                            return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
                        }
                ));
    }

    @Override
    public ChatRoomMember getOpponentMember(Long roomId, Long userId) {
        List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findActiveMembersByRoomId(roomId);
        return activeMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ChatException(ChatErrorCode.INTERNAL_ERROR, "Opponent member not found"));
    }
}
