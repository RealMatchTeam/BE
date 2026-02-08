package com.example.RealMatch.chat.application.service.room;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.chat.application.util.ChatConstants;
import com.example.RealMatch.chat.application.util.ChatRoomValidator;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
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
    private final BrandRepository brandRepository;

    @Override
    public OpponentInfo getOpponentInfo(Long opponentUserId, ChatRoomMemberRole role) {
        if (opponentUserId == null) {
            return unknownOpponentInfo(null);
        }

        User user = userRepository.findById(opponentUserId).orElse(null);
        if (user == null) {
            return unknownOpponentInfo(opponentUserId);
        }

        Brand brand = null;
        if (role == ChatRoomMemberRole.BRAND) {
            brand = brandRepository.findByUserId(opponentUserId).orElse(null);
        }

        return toOpponentInfo(opponentUserId, role, user, brand);
    }

    @Override
    public Map<Long, OpponentInfo> getOpponentInfoMapBatch(Long userId, List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<ChatRoomMember>> opponentByRoom = findOpponentMembersByRoom(userId, roomIds);
        Map<Long, OpponentMember> roomToOpponentMemberMap = buildOpponentMemberMap(roomIds, opponentByRoom);
        Map<Long, User> userMap = loadOpponentUsers(roomToOpponentMemberMap);
        Map<Long, Brand> brandMap = loadOpponentBrands(roomToOpponentMemberMap);

        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> toOpponentInfo(roomToOpponentMemberMap.get(roomId), userMap, brandMap)
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

    private Map<Long, OpponentMember> buildOpponentMemberMap(
            List<Long> roomIds,
            Map<Long, List<ChatRoomMember>> opponentByRoom
    ) {
        return roomIds.stream()
                .collect(Collectors.toMap(
                        roomId -> roomId,
                        roomId -> {
                            List<ChatRoomMember> members = opponentByRoom.get(roomId);
                            ChatRoomValidator.validateDirectRoomOpponent(members, roomId);
                            ChatRoomMember member = members.getFirst();
                            return new OpponentMember(member.getUserId(), member.getRole());
                        }
                ));
    }

    private Map<Long, User> loadOpponentUsers(Map<Long, OpponentMember> roomToOpponentMemberMap) {
        Set<Long> opponentUserIds = roomToOpponentMemberMap.values().stream()
                .map(OpponentMember::userId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        if (opponentUserIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(opponentUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private Map<Long, Brand> loadOpponentBrands(Map<Long, OpponentMember> roomToOpponentMemberMap) {
        List<Long> brandUserIds = roomToOpponentMemberMap.values().stream()
                .filter(member -> member.role() == ChatRoomMemberRole.BRAND)
                .map(OpponentMember::userId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (brandUserIds.isEmpty()) {
            return Map.of();
        }
        return brandRepository.findByUserIdIn(brandUserIds).stream()
                .collect(Collectors.toMap(b -> b.getUser().getId(), b -> b));
    }

    private OpponentInfo toOpponentInfo(
            OpponentMember opponentMember,
            Map<Long, User> userMap,
            Map<Long, Brand> brandMap
    ) {
        if (opponentMember == null || opponentMember.userId() == null) {
            return unknownOpponentInfo(null);
        }
        User user = userMap.get(opponentMember.userId());
        if (user == null) {
            return unknownOpponentInfo(opponentMember.userId());
        }
        Brand brand = brandMap.get(opponentMember.userId());
        return toOpponentInfo(opponentMember.userId(), opponentMember.role(), user, brand);
    }

    private OpponentInfo toOpponentInfo(Long opponentUserId, ChatRoomMemberRole role, User user, Brand brand) {
        if (role == ChatRoomMemberRole.BRAND) {
            String brandName = brand != null ? brand.getBrandName() : ChatConstants.UNKNOWN_OPPONENT_NAME;
            String brandImageUrl = brand != null ? brand.getLogoUrl() : null;
            String profileImageUrl = brandImageUrl != null ? brandImageUrl : user.getProfileImageUrl();
            return new OpponentInfo(opponentUserId, brandName, profileImageUrl);
        }
        return new OpponentInfo(opponentUserId, user.getNickname(), user.getProfileImageUrl());
    }

    private OpponentInfo unknownOpponentInfo(Long opponentUserId) {
        return new OpponentInfo(opponentUserId, ChatConstants.UNKNOWN_OPPONENT_NAME, null);
    }

    private record OpponentMember(Long userId, ChatRoomMemberRole role) {
    }
}
