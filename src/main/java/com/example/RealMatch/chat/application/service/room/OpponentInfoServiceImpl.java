package com.example.RealMatch.chat.application.service.room;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

    private static final String UNKNOWN_OPPONENT_NAME = "알 수 없음";

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    @Override
    public OpponentInfo getOpponentInfo(Long opponentUserId) {
        if (opponentUserId == null) {
            return new OpponentInfo(null, UNKNOWN_OPPONENT_NAME, null);
        }

        User user = userRepository.findById(opponentUserId).orElse(null);
        if (user == null) {
            return new OpponentInfo(opponentUserId, UNKNOWN_OPPONENT_NAME, null);
        }

        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
    }

    @Override
    public Map<Long, OpponentInfo> getOpponentInfoMapBatch(Long userId, List<Long> roomIds) {
        List<ChatRoomMember> opponentMembers = chatRoomMemberRepository
                .findByRoomIdIn(roomIds).stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .toList();

        Map<Long, List<ChatRoomMember>> opponentByRoom = opponentMembers.stream()
                .collect(Collectors.groupingBy(ChatRoomMember::getRoomId));

        // 1:1 채팅방 검증
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

        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> {
                            Long opponentUserId = roomToOpponentUserIdMap.get(roomId);
                            User user = userMap.get(opponentUserId);
                            if (user == null) {
                                return new OpponentInfo(opponentUserId, UNKNOWN_OPPONENT_NAME, null);
                            }

                            return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
                        }
                ));
    }

    @Override
    public ChatRoomMember getOpponentMember(Long roomId, Long userId) {
        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByRoomId(roomId);
        return allMembers.stream()
                .filter(m -> !m.getUserId().equals(userId) && !m.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ChatException(ChatErrorCode.INTERNAL_ERROR, "Opponent member not found"));
    }
}
