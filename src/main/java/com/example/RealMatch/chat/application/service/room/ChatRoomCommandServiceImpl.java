package com.example.RealMatch.chat.application.service.room;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.cache.ChatCacheInvalidationService;
import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.application.util.ChatRoomKeyGenerator;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageEventPublisher eventPublisher;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatCacheInvalidationService cacheInvalidationService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatRoomCreateResponse createOrGetRoomAsMember(Long userId, Long brandId, Long creatorId) {
        validateMemberRequest(userId, brandId, creatorId);
        return findOrCreateRoom(brandId, creatorId);
    }

    @Override
    @Transactional
    public ChatRoomCreateResponse createOrGetRoomSystem(Long brandId, Long creatorId) {
        validateSystemRequest(brandId, creatorId);
        return findOrCreateRoom(brandId, creatorId);
    }

    private void validateMemberRequest(Long userId, Long brandId, Long creatorId) {
        validateSystemRequest(brandId, creatorId);
        if (userId == null || !userId.equals(brandId) && !userId.equals(creatorId)) {
            throw new CustomException(ChatErrorCode.NOT_ROOM_MEMBER);
        }
    }

    private void validateSystemRequest(Long brandId, Long creatorId) {
        if (brandId == null || creatorId == null || brandId.equals(creatorId)) {
            throw new CustomException(ChatErrorCode.INVALID_ROOM_REQUEST);
        }
    }

    private ChatRoomCreateResponse findOrCreateRoom(Long brandId, Long creatorId) {
        // ponytail: serialize room creation per brand; use an atomic room-key upsert if contention matters.
        userRepository.findByIdForUpdate(brandId)
                .filter(user -> !user.isDeleted() && user.getRole() == Role.BRAND)
                .orElseThrow(() -> new CustomException(ChatErrorCode.INVALID_ROOM_REQUEST));
        validateUserRole(creatorId, Role.CREATOR);
        String roomKey = ChatRoomKeyGenerator.createDirectRoomKey(brandId, creatorId);

        ChatRoom room = chatRoomRepository.findByRoomKeyForUpdate(roomKey).orElse(null);
        if (room == null) {
            room = createRoomWithMembers(roomKey, brandId, creatorId);
        }

        return new ChatRoomCreateResponse(
                room.getId(),
                room.getRoomKey(),
                room.getCreatedAt()
        );
    }

    private void validateUserRole(Long userId, Role expectedRole) {
        userRepository.findById(userId)
                .filter(user -> !user.isDeleted() && user.getRole() == expectedRole)
                .orElseThrow(() -> new CustomException(ChatErrorCode.INVALID_ROOM_REQUEST));
    }

    private ChatRoom createRoomWithMembers(
            String roomKey,
            Long brandId,
            Long creatorId
    ) {
        ChatRoom room = createRoom(roomKey);

        createMemberIfNotExists(room.getId(), brandId, ChatRoomMemberRole.BRAND);
        createMemberIfNotExists(room.getId(), creatorId, ChatRoomMemberRole.CREATOR);

        afterCommitExecutor.execute(() -> {
            cacheInvalidationService.invalidateAfterRoomCreated(brandId, creatorId);
        });

        return room;
    }

    private ChatRoom createRoom(String roomKey) {
        ChatRoom newRoom = chatRoomRepository.saveAndFlush(ChatRoom.createDirectRoom(roomKey));
        afterCommitExecutor.execute(() -> eventPublisher.publishRoomListUpdated(newRoom.getId()));
        return newRoom;
    }

    private void createMemberIfNotExists(Long roomId, Long userId, ChatRoomMemberRole role) {
        if (chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).isPresent()) {
            return;
        }

        chatRoomMemberRepository.saveAndFlush(ChatRoomMember.create(roomId, userId, role));
    }

}
