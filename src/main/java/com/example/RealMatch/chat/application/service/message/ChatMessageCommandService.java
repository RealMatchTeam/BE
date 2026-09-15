package com.example.RealMatch.chat.application.service.message;

import static com.example.RealMatch.attachment.code.AttachmentErrorCode.INVALID_FILE_TYPE;
import static com.example.RealMatch.chat.domain.enums.ChatMessageType.FILE;
import static com.example.RealMatch.chat.domain.enums.ChatMessageType.IMAGE;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.chat.application.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.application.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.application.dto.websocket.ChatSendMessageCommand;
import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.repository.ChatMessageRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomRepository;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.application.util.MessagePreviewGenerator;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatMessageCommandService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageEventPublisher eventPublisher;
    private final AttachmentQueryService attachmentQueryService;
    private final ChatRoomMemberService chatRoomMemberService;
    private final MessagePreviewGenerator messagePreviewGenerator;
    private final ChatMessageResponseMapper responseMapper;
    private final SystemMessagePayloadSerializer payloadSerializer;
    private final AfterCommitExecutor afterCommitExecutor;

    @Transactional
    @NonNull
    public ChatMessageResponse saveMessage(ChatSendMessageCommand command, Long senderId) {
        // Room 존재 여부 및 멤버 권한 검증
        validateRoomId(command.roomId());
        if (command.clientMessageId() == null || command.clientMessageId().isBlank()
                || command.clientMessageId().length() > 36) {
            throw new CustomException(ChatErrorCode.IDEMPOTENCY_CONFLICT);
        }
        chatRoomMemberService.getActiveMemberOrThrow(command.roomId(), senderId);
        chatRoomRepository.findByIdForUpdate(command.roomId())
                .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));

        // 멱등성 처리: 이미 저장된 메시지가 있는지 확인
        Optional<ChatMessageResponse> idempotentResponse = handleIdempotency(command, senderId);
        if (idempotentResponse.isPresent()) {
            return idempotentResponse.get();
        }

        // 신규 메시지 생성 및 저장
        return createAndSaveMessage(command, senderId);
    }

    private void validateRoomId(Long roomId) {
        if (roomId == null) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
    }

    private Optional<ChatMessageResponse> handleIdempotency(
            ChatSendMessageCommand command,
            Long senderId
    ) {
        ChatMessage existing = chatMessageRepository
                .findByClientMessageIdAndSenderId(command.clientMessageId(), senderId)
                .orElse(null);

        if (existing != null) {
            validateIdempotentConsistency(existing, command);
            AttachmentDto existingAttachment = getAndValidateAttachment(
                    existing.getAttachmentId(), senderId, existing.getMessageType());
            return Optional.of(responseMapper.toResponse(existing, existingAttachment));
        }

        return Optional.empty();
    }

    private ChatMessageResponse createAndSaveMessage(
            ChatSendMessageCommand command,
            Long senderId
    ) {
        AttachmentDto attachment = getAndValidateAttachment(command.attachmentId(), senderId, command.messageType());

        // 메시지 생성
        ChatMessage message;
        try {
            message = ChatMessage.createUserMessage(
                    command.roomId(),
                    senderId,
                    command.messageType(),
                    command.content(),
                    command.attachmentId(),
                    command.clientMessageId()
            );
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage(), ex);
        }

        // 메시지 저장 (동시성 처리 포함)
        ChatMessage saved = chatMessageRepository.saveAndFlush(message);

        // 채팅방 마지막 메시지 업데이트
        updateChatRoomLastMessage(saved);

        // 응답 생성
        ChatMessageResponse response = responseMapper.toResponse(saved, attachment);
        publishAfterCommit(response);
        return response;
    }

    @Transactional
    @NonNull
    public ChatMessageResponse saveSystemMessage(
            Long roomId,
            String eventId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        // 방 존재 여부 검증
        validateRoomId(roomId);
        if (eventId == null || eventId.isBlank() || eventId.length() > 100) {
            throw new IllegalArgumentException("System event id must contain 1 to 100 characters.");
        }
        chatRoomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.ROOM_NOT_FOUND));
        ChatMessage existing = chatMessageRepository.findByRoomIdAndSystemEventId(roomId, eventId).orElse(null);
        if (existing != null) {
            if (existing.getSystemKind() != kind) {
                throw new CustomException(ChatErrorCode.IDEMPOTENCY_CONFLICT);
            }
            return responseMapper.toResponse(existing, null);
        }

        // 시스템 메시지 생성
        ChatMessage message;
        try {
            message = ChatMessage.createSystemMessage(
                    roomId,
                    eventId,
                    kind,
                    payloadSerializer.serialize(payload)
            );
        } catch (IllegalArgumentException ex) {
            throw new CustomException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage(), ex);
        }

        // 메시지 저장
        ChatMessage saved = chatMessageRepository.saveAndFlush(message);
        updateChatRoomLastMessage(saved);

        ChatMessageResponse response = responseMapper.toResponse(saved, null);
        publishAfterCommit(response);
        return response;
    }

    private AttachmentDto getAndValidateAttachment(Long attachmentId, Long userId, ChatMessageType type) {
        if (attachmentId == null) {
            return null;
        }
        if (type != IMAGE && type != FILE) {
            throw new CustomException(INVALID_FILE_TYPE);
        }
        return attachmentQueryService.retainForChat(attachmentId, userId,
                AttachmentType.valueOf(type.name()));
    }

    private void validateIdempotentConsistency(ChatMessage stored, ChatSendMessageCommand command) {
        if (!Objects.equals(stored.getRoomId(), command.roomId())) {
            throw new CustomException(ChatErrorCode.INVALID_ROOM_FOR_MESSAGE);
        }
        if (!Objects.equals(stored.getAttachmentId(), command.attachmentId())
                || !Objects.equals(stored.getMessageType(), command.messageType())
                || !Objects.equals(stored.getContent(), command.content())) {
            throw new CustomException(ChatErrorCode.IDEMPOTENCY_CONFLICT);
        }
    }

    private void updateChatRoomLastMessage(ChatMessage message) {
        String preview = messagePreviewGenerator.generate(message.getMessageType(), message.getContent());
        int updatedRows = chatRoomRepository.updateLastMessageIfNewer(
                message.getRoomId(),
                message.getId(),
                message.getCreatedAt(),
                preview,
                message.getMessageType()
        );
        if (updatedRows == 0 && !chatRoomRepository.existsById(message.getRoomId())) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        if (updatedRows > 1) {
            LOG.warn("Unexpected chat room update count. roomId={}, messageId={}, rows={}",
                    message.getRoomId(), message.getId(), updatedRows);
        }
    }

    private void publishAfterCommit(ChatMessageResponse response) {
        afterCommitExecutor.execute(() -> {
            eventPublisher.publishMessageCreated(response.roomId(), response);
            eventPublisher.publishRoomListUpdated(response.roomId());
        });
    }
}
