package com.example.RealMatch.chat.application.service.message;

import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.service.room.ChatRoomUpdateService;
import com.example.RealMatch.chat.domain.entity.ChatAttachment;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.repository.ChatAttachmentRepository;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

@Service
public class ChatMessageCommandServiceImpl implements ChatMessageCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatAttachmentRepository chatAttachmentRepository;
    private final ChatRoomUpdateService chatRoomUpdateService;
    private final MessagePreviewGenerator messagePreviewGenerator;
    private final ChatMessageResponseMapper responseMapper;
    private final SystemMessagePayloadSerializer payloadSerializer;

    public ChatMessageCommandServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ChatAttachmentRepository chatAttachmentRepository,
            ChatRoomUpdateService chatRoomUpdateService,
            MessagePreviewGenerator messagePreviewGenerator,
            SystemMessagePayloadSerializer payloadSerializer
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatAttachmentRepository = chatAttachmentRepository;
        this.chatRoomUpdateService = chatRoomUpdateService;
        this.messagePreviewGenerator = messagePreviewGenerator;
        this.payloadSerializer = payloadSerializer;
        this.responseMapper = new ChatMessageResponseMapper(payloadSerializer);
    }

    @Override
    @Transactional
    @NonNull
    @SuppressWarnings("null")
    public ChatMessageResponse saveMessage(ChatSendMessageCommand command, Long senderId) {
        validateCommand(command, senderId);

        // 첨부 파일 존재 검증
        if (command.attachmentId() != null) {
            chatAttachmentRepository.findById(command.attachmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + command.attachmentId()));
        }

        ChatMessage existing = chatMessageRepository
                .findByClientMessageIdAndSenderId(command.clientMessageId(), senderId)
                .orElse(null);
        if (existing != null) {
            ChatAttachment attachment = loadAttachmentIfNeeded(existing);
            return responseMapper.toResponse(existing, attachment);
        }

        ChatMessage message = ChatMessage.createUserMessage(
                command.roomId(),
                senderId,
                command.messageType(),
                command.content(),
                command.attachmentId(),
                command.clientMessageId()
        );
        ChatMessage saved = chatMessageRepository.save(message);
        updateChatRoomLastMessage(saved);
        return responseMapper.toResponse(saved, loadAttachmentIfNeeded(saved));
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public ChatMessageResponse saveSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        ChatMessage message = ChatMessage.createSystemMessage(
                roomId,
                kind,
                payloadSerializer.serialize(payload)
        );
        ChatMessage saved = chatMessageRepository.save(message);
        updateChatRoomLastMessage(saved);
        return responseMapper.toResponse(saved, null);
    }

    private void validateCommand(ChatSendMessageCommand command, Long senderId) {
        if (senderId == null) {
            throw new IllegalArgumentException("Sender id is required.");
        }

        ChatMessageType messageType = command.messageType();
        if (messageType == ChatMessageType.TEXT) {
            if (command.content() == null || command.content().isBlank()) {
                throw new IllegalArgumentException("Content is required for TEXT messages.");
            }
        } else if (messageType == ChatMessageType.IMAGE || messageType == ChatMessageType.FILE) {
            if (command.attachmentId() == null) {
                throw new IllegalArgumentException("Attachment id is required for IMAGE/FILE messages.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported message type for user send.");
        }
    }

    private ChatAttachment loadAttachmentIfNeeded(ChatMessage message) {
        Long attachmentId = message.getAttachmentId();
        if (attachmentId == null) {
            return null;
        }
        return chatAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
    }

    private void updateChatRoomLastMessage(ChatMessage message) {
        String preview = messagePreviewGenerator.generate(message.getMessageType(), message.getContent());
        Long roomId = Objects.requireNonNull(message.getRoomId(), "Room id must not be null.");
        Long messageId = Objects.requireNonNull(message.getId(), "Message id must not be null after save.");
        chatRoomUpdateService.updateLastMessage(
                roomId,
                messageId,
                message.getCreatedAt(),
                message.getMessageType(),
                preview
        );
    }
}
