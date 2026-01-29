package com.example.RealMatch.chat.application.service.message;

import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.application.util.ChatExceptionConverter;
import com.example.RealMatch.chat.application.util.MessagePreviewGenerator;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandServiceImpl implements ChatMessageCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final AttachmentQueryService attachmentQueryService;
    private final ChatRoomMemberService chatRoomMemberService;
    private final ChatRoomCommandService chatRoomCommandService;
    private final MessagePreviewGenerator messagePreviewGenerator;
    private final ChatMessageResponseMapper responseMapper;
    private final SystemMessagePayloadSerializer payloadSerializer;

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse saveMessage(ChatSendMessageCommand command, Long senderId) {
        // Room 존재 여부 및 멤버 권한 검증
        validateRoomId(command.roomId());
        chatRoomMemberService.getActiveMemberOrThrow(command.roomId(), senderId);

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
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
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
                    existing.getAttachmentId(), senderId);
            return Optional.of(responseMapper.toResponse(existing, existingAttachment));
        }

        return Optional.empty();
    }

    private ChatMessageResponse createAndSaveMessage(
            ChatSendMessageCommand command,
            Long senderId
    ) {
        AttachmentDto attachment = getAndValidateAttachment(command.attachmentId(), senderId);

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
            throw ChatExceptionConverter.convert(ex);
        }

        // 메시지 저장 (동시성 처리 포함)
        ChatMessage saved = saveMessageWithIdempotencyCheck(message, command, senderId);

        // 채팅방 마지막 메시지 업데이트
        updateChatRoomLastMessage(saved);

        // 응답 생성
        AttachmentDto savedAttachment = attachment != null
                ? attachment
                : attachmentQueryService.findById(saved.getAttachmentId());
        return responseMapper.toResponse(saved, savedAttachment);
    }

    private ChatMessage saveMessageWithIdempotencyCheck(
            ChatMessage message,
            ChatSendMessageCommand command,
            Long senderId
    ) {
        try {
            return chatMessageRepository.save(message);
        } catch (DataIntegrityViolationException ex) {
            // 다른 트랜잭션에서 이미 저장된 경우, 기존 메시지를 반환
            ChatMessage duplicateMessage = chatMessageRepository
                    .findByClientMessageIdAndSenderId(command.clientMessageId(), senderId)
                    .orElseThrow(() -> ex);

            validateIdempotentConsistency(duplicateMessage, command);
            return duplicateMessage;
        }
    }

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse saveSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        // 방 존재 여부 검증
        validateRoomId(roomId);

        // 시스템 메시지 생성
        ChatMessage message;
        try {
            message = ChatMessage.createSystemMessage(
                    roomId,
                    kind,
                    payloadSerializer.serialize(payload)
            );
        } catch (IllegalArgumentException ex) {
            throw ChatExceptionConverter.convert(ex);
        }

        // 메시지 저장
        ChatMessage saved = chatMessageRepository.save(message);
        updateChatRoomLastMessage(saved);

        return responseMapper.toResponse(saved, null);
    }

    private AttachmentDto getAndValidateAttachment(Long attachmentId, Long userId) {
        if (attachmentId == null) {
            return null;
        }
        attachmentQueryService.validateOwnership(attachmentId, userId);
        return attachmentQueryService.findById(attachmentId);
    }

    private void validateIdempotentConsistency(ChatMessage stored, ChatSendMessageCommand command) {
        if (!Objects.equals(stored.getRoomId(), command.roomId())) {
            throw new ChatException(ChatErrorCode.INVALID_ROOM_FOR_MESSAGE);
        }
        if (!Objects.equals(stored.getAttachmentId(), command.attachmentId())
                || !Objects.equals(stored.getMessageType(), command.messageType())
                || !Objects.equals(stored.getContent(), command.content())) {
            throw new ChatException(ChatErrorCode.IDEMPOTENCY_CONFLICT);
        }
    }

    private void updateChatRoomLastMessage(ChatMessage message) {
        String preview = messagePreviewGenerator.generate(message.getMessageType(), message.getContent());
        chatRoomCommandService.updateLastMessage(
                message.getRoomId(),
                message.getId(),
                message.getCreatedAt(),
                message.getMessageType(),
                preview
        );
    }
}
