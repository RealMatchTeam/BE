package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomCommandServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    @Transactional
    public ChatRoomCreateResponse createOrGetRoom(Long userId, ChatRoomCreateRequest request) {
        Long brandId = request.brandId();
        Long creatorId = request.creatorId();

        validateRequest(userId, brandId, creatorId);

        String roomKey = generateRoomKey(brandId, creatorId);

        // 제안 없이 채팅을 시작한 경우, 채팅을 시작한 주체에 따라 초기 lastProposalDirection 설정
        // 이후 제안 메시지가 전송되면 해당 방향으로 업데이트됨
        ChatProposalDirection initialDirection = userId.equals(brandId)
                ? ChatProposalDirection.BRAND_TO_CREATOR
                : ChatProposalDirection.CREATOR_TO_BRAND;

        ChatRoom room = chatRoomRepository.findByRoomKey(roomKey)
                .orElseGet(() -> createRoomWithMembers(roomKey, brandId, creatorId, initialDirection));

        return new ChatRoomCreateResponse(
                room.getId(),
                room.getRoomKey(),
                room.getLastProposalDirection(),
                room.getCreatedAt()
        );
    }

    private void validateRequest(Long userId, Long brandId, Long creatorId) {
        if (brandId == null || creatorId == null || brandId.equals(creatorId)) {
            throw new ChatException(ChatErrorCode.INVALID_ROOM_REQUEST);
        }
        if (!userId.equals(brandId) && !userId.equals(creatorId)) {
            throw new ChatException(ChatErrorCode.NOT_ROOM_MEMBER);
        }
    }

    private String generateRoomKey(Long brandId, Long creatorId) {
        long smallerId = Math.min(brandId, creatorId);
        long largerId = Math.max(brandId, creatorId);
        return String.format("direct:%d:%d", smallerId, largerId);
    }

    private ChatRoom createRoomWithMembers(
            String roomKey,
            Long brandId,
            Long creatorId,
            ChatProposalDirection initialDirection
    ) {
        ChatRoom room;
        try {
            // 제안 없이 채팅을 시작한 경우, 채팅을 시작한 주체에 따라 초기 lastProposalDirection 설정
            // 이후 제안 메시지가 전송되면 해당 방향으로 업데이트됨
            // 브랜드가 시작 → BRAND_TO_CREATOR, 크리에이터가 시작 → CREATOR_TO_BRAND
            room = chatRoomRepository.saveAndFlush(
                    ChatRoom.createDirectRoom(roomKey, initialDirection)
            );
        } catch (DataIntegrityViolationException e) {
            // 다른 스레드가 이미 채팅방을 생성한 경우, 기존 방을 조회
            room = chatRoomRepository.findByRoomKey(roomKey)
                    .orElseThrow(() -> new ChatException(ChatErrorCode.INTERNAL_ERROR));
        }

        createMemberIfNotExists(room.getId(), brandId, ChatRoomMemberRole.BRAND);
        createMemberIfNotExists(room.getId(), creatorId, ChatRoomMemberRole.CREATOR);

        return room;
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
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        room.updateLastMessage(messageId, messageAt, messagePreview, messageType);
    }

    @Override
    @Transactional
    public void updateProposalDirection(@NonNull Long roomId, @NonNull ChatProposalDirection direction) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        room.updateProposalDirection(direction);
    }
}
