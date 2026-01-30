package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.application.util.ChatRoomKeyGenerator;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomCommandServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageEventPublisher eventPublisher;
    private final AfterCommitExecutor afterCommitExecutor;

    @Override
    @Transactional
    public ChatRoomCreateResponse createOrGetRoom(Long userId, Long brandId, Long creatorId) {
        validateRequest(userId, brandId, creatorId);

        String roomKey = ChatRoomKeyGenerator.createDirectRoomKey(brandId, creatorId);

        ChatRoom room = chatRoomRepository.findByRoomKey(roomKey)
                .orElseGet(() -> createRoomWithMembers(roomKey, brandId, creatorId));

        return new ChatRoomCreateResponse(
                room.getId(),
                room.getRoomKey(),
                room.getCreatedAt()
        );
    }

    private void validateRequest(Long userId, Long brandId, Long creatorId) {
        if (brandId == null || creatorId == null || brandId.equals(creatorId)) {
            throw new CustomException(ChatErrorCode.INVALID_ROOM_REQUEST);
        }
        if (!userId.equals(brandId) && !userId.equals(creatorId)) {
            throw new CustomException(ChatErrorCode.NOT_ROOM_MEMBER);
        }
    }

    private ChatRoom createRoomWithMembers(
            String roomKey,
            Long brandId,
            Long creatorId
    ) {
        // 먼저 조회 시도 (대부분의 경우 기존 방이 있을 것)
        ChatRoom room = chatRoomRepository.findByRoomKey(roomKey).orElse(null);

        if (room == null) {
            // 방이 없는 경우에만 생성 시도
            room = createRoomWithRetry(roomKey);
        }

        // 멤버 생성 (멤버는 중복 생성 방지 로직이 있음)
        createMemberIfNotExists(room.getId(), brandId, ChatRoomMemberRole.BRAND);
        createMemberIfNotExists(room.getId(), creatorId, ChatRoomMemberRole.CREATOR);

        return room;
    }

    private ChatRoom createRoomWithRetry(String roomKey) {
        try {
            ChatRoom newRoom = chatRoomRepository.saveAndFlush(
                    ChatRoom.createDirectRoom(roomKey)
            );
            // 새로 생성된 채팅방인 경우, 양쪽 사용자에게 채팅방 목록 업데이트 알림
            afterCommitExecutor.execute(() -> {
                eventPublisher.publishRoomListUpdated(newRoom.getId());
            });
            return newRoom;
        } catch (DataIntegrityViolationException e) {
            // 다른 스레드가 이미 채팅방을 생성한 경우, 기존 방을 조회
            return chatRoomRepository.findByRoomKey(roomKey)
                    .orElseThrow(() -> new CustomException(ChatErrorCode.INTERNAL_ERROR));
        }
    }

    private void createMemberIfNotExists(Long roomId, Long userId, ChatRoomMemberRole role) {
        if (chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).isPresent()) {
            return;
        }

        try {
            chatRoomMemberRepository.saveAndFlush(
                    ChatRoomMember.create(roomId, userId, role)
            );
        } catch (DataIntegrityViolationException e) {
            // 동시성 상황에서 다른 스레드가 이미 멤버를 생성한 경우 무시
            LOG.debug("createMemberIfNotExists ignored. roomId={}, userId={}, role={}", roomId, userId, role, e);
        }
    }

    @Override
    @Transactional
    public void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            ChatMessageType messageType,
            String messagePreview
    ) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

        room.updateLastMessage(messageId, messageAt, messagePreview, messageType);
    }

    @Override
    @Transactional
    public void updateProposalStatusByUsers(
            @NonNull Long brandUserId,
            @NonNull Long creatorUserId,
            @NonNull ChatProposalStatus status
    ) {
        String roomKey = ChatRoomKeyGenerator.createDirectRoomKey(brandUserId, creatorUserId);
        ChatRoom room = chatRoomRepository.findByRoomKey(roomKey).orElse(null);

        if (room == null) {
            LOG.warn("Chat room not found for proposal status update. brandUserId={}, creatorUserId={}, status={}",
                    brandUserId, creatorUserId, status);
            return;
        }

        room.updateProposalStatus(status);
        LOG.debug("Chat room proposal status updated. roomId={}, status={}", room.getId(), status);
    }
}
