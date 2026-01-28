package com.example.RealMatch.chat.application.service.message;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.util.ChatRoomMemberValidator;
import com.example.RealMatch.chat.application.util.MessagePreviewGenerator;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
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
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomCommandService chatRoomCommandService;
    private final MessagePreviewGenerator messagePreviewGenerator;
    private final ChatMessageResponseMapper responseMapper;
    private final SystemMessagePayloadSerializer payloadSerializer;

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse saveMessage(ChatSendMessageCommand command, Long senderId) {
        // Room 존재 여부 및 멤버 권한 검증
        if (command.roomId() == null) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        ChatRoomMember member = chatRoomMemberRepository
                .findMemberByRoomIdAndUserIdWithRoomCheck(command.roomId(), senderId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_ROOM_MEMBER));
        
        // 활성 상태 검증
        ChatRoomMemberValidator.validateActiveMember(member);

        // 멱등성을 위해 선조회한다. (이미 저장된 메시지가 있나?)
        ChatMessage existing = chatMessageRepository
                .findByClientMessageIdAndSenderId(command.clientMessageId(), senderId)
                .orElse(null);
        if (existing != null) {
            validateIdempotentConsistency(existing, command);
            AttachmentDto existingAttachment = getAndValidateAttachment(existing.getAttachmentId(), senderId);
            return responseMapper.toResponse(existing, existingAttachment);
        }

        AttachmentDto attachment = getAndValidateAttachment(command.attachmentId(), senderId);

        // 신규 메시지 생성 및 저장 시도
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
            throw new ChatException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage());
        }
        ChatMessage saved;
        try {
            saved = chatMessageRepository.save(message);
        } catch (DataIntegrityViolationException ex) {
            // 다른 트랜잭션에서 이미 저장됐다면, 중복 저장을 방지하기 위해 기존 메시지를 반환한다.
            ChatMessage duplicateMessage = chatMessageRepository
                    .findByClientMessageIdAndSenderId(command.clientMessageId(), senderId)
                    .orElseThrow(() -> ex);
            
            validateIdempotentConsistency(duplicateMessage, command);
            AttachmentDto duplicateAttachment = getAndValidateAttachment(duplicateMessage.getAttachmentId(), senderId);
            return responseMapper.toResponse(duplicateMessage, duplicateAttachment);
        }

        updateChatRoomLastMessage(saved);
        AttachmentDto savedAttachment = attachment != null 
                ? attachment 
                : attachmentQueryService.findById(saved.getAttachmentId());
        return responseMapper.toResponse(saved, savedAttachment);
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
        if (roomId == null) {
            throw new ChatException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        
        ChatMessage message;
        try {
            message = ChatMessage.createSystemMessage(
                    roomId,
                    kind,
                    payloadSerializer.serialize(payload)
            );
        } catch (IllegalArgumentException ex) {
            throw new ChatException(ChatErrorCode.INVALID_MESSAGE_FORMAT, ex.getMessage());
        }
        
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
