package com.example.RealMatch.chat.application.service.room;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.util.ChatConstants;
import com.example.RealMatch.chat.application.util.ChatRoomValidator;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.global.exception.CustomException;
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
            return unknownOpponentInfo(null);
        }

        User user = userRepository.findById(opponentUserId).orElse(null);
        if (user == null) {
            return unknownOpponentInfo(opponentUserId);
        }

        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
    }

    @Override
    public Map<Long, OpponentInfo> getOpponentInfoMapBatch(Long userId, List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<ChatRoomMember>> opponentByRoom = findOpponentMembersByRoom(userId, roomIds);
        Map<Long, Long> roomToOpponentUserIdMap = buildOpponentUserIdMap(roomIds, opponentByRoom);
        Map<Long, User> userMap = loadOpponentUsers(roomToOpponentUserIdMap);

        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> toOpponentInfo(roomToOpponentUserIdMap.get(roomId), userMap)
                ));
    }

    @Override
    public ChatRoomMember getOpponentMember(Long roomId, Long userId) {
        List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findActiveMembersByRoomId(roomId);
        return activeMembers.stream()
                .filter(m -> !m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ChatErrorCode.INTERNAL_ERROR, "Opponent member not found"));
    }

    private Map<Long, List<ChatRoomMember>> findOpponentMembersByRoom(Long userId, List<Long> roomIds) {
        List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findActiveMembersByRoomIdIn(roomIds);
        return activeMembers.stream()
                .filter(member -> !member.getUserId().equals(userId))
                .collect(Collectors.groupingBy(ChatRoomMember::getRoomId));
    }

    private Map<Long, Long> buildOpponentUserIdMap(
            List<Long> roomIds,
            Map<Long, List<ChatRoomMember>> opponentByRoom
    ) {
        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> {
                            List<ChatRoomMember> members = opponentByRoom.get(roomId);
                            ChatRoomValidator.validateDirectRoomOpponent(members, roomId);
                            return members.getFirst().getUserId();
                        }
                ));
    }

    private Map<Long, User> loadOpponentUsers(Map<Long, Long> roomToOpponentUserIdMap) {
        Set<Long> opponentUserIds = new HashSet<>(roomToOpponentUserIdMap.values());
        if (opponentUserIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(opponentUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private OpponentInfo toOpponentInfo(Long opponentUserId, Map<Long, User> userMap) {
        if (opponentUserId == null) {
            return unknownOpponentInfo(null);
        }
        User user = userMap.get(opponentUserId);
        if (user == null) {
            return unknownOpponentInfo(opponentUserId);
        }
        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
    }

    private OpponentInfo unknownOpponentInfo(Long opponentUserId) {
        return new OpponentInfo(opponentUserId, ChatConstants.UNKNOWN_OPPONENT_NAME, null);
    }
}
