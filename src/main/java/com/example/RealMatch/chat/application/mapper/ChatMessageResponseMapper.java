package com.example.RealMatch.chat.application.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.application.dto.AttachmentDto;
import com.example.RealMatch.attachment.presentation.dto.response.AttachmentInfoResponse;
import com.example.RealMatch.chat.application.util.ChatConstants;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSenderType;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessageResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessageResponseMapper {

    private static final Logger LOG = LoggerFactory.getLogger(ChatMessageResponseMapper.class);

    private final SystemMessagePayloadSerializer payloadSerializer;

    public ChatMessageResponse toResponse(ChatMessage message, AttachmentDto attachment) {
        AttachmentInfoResponse attachmentResponse = attachment != null 
                ? toAttachmentInfoResponse(attachment) 
                : null;
        
        ChatMessageType messageType = message.getMessageType();
        return new ChatMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                messageType == ChatMessageType.SYSTEM ? ChatSenderType.SYSTEM : ChatSenderType.USER,
                messageType,
                message.getContent(),
                attachmentResponse,
                messageType == ChatMessageType.SYSTEM ? toSystemMessageResponse(message) : null,
                message.getCreatedAt(),
                message.getClientMessageId()
        );
    }

    private AttachmentInfoResponse toAttachmentInfoResponse(AttachmentDto attachment) {
        return new AttachmentInfoResponse(
                attachment.attachmentId(),
                attachment.attachmentType(),
                attachment.contentType(),
                attachment.originalName(),
                attachment.fileSize(),
                attachment.accessUrl(),
                attachment.status()
        );
    }

    private ChatSystemMessageResponse toSystemMessageResponse(ChatMessage message) {
        ChatSystemMessageKind kind = message.getSystemKind();
        String rawPayload = message.getSystemPayload();

        try {
            ChatSystemMessagePayload payload = payloadSerializer.deserialize(kind, rawPayload);
            return new ChatSystemMessageResponse(
                    ChatConstants.SYSTEM_MESSAGE_SCHEMA_VERSION,
                    kind,
                    payload
            );
        } catch (Exception ex) {
            logDeserializationError(message.getId(), kind, rawPayload, ex);
            return null;
        }
    }

    private void logDeserializationError(
            Long messageId,
            ChatSystemMessageKind kind,
            String rawPayload,
            Exception ex
    ) {
        int payloadLength = rawPayload != null ? rawPayload.length() : 0;
        String payloadHash = rawPayload != null
                ? Integer.toHexString(rawPayload.hashCode())
                : "null";
        LOG.error(
                "Failed to deserialize system message payload. messageId={}, kind={}, payloadLength={}, payloadHash={}",
                messageId, kind, payloadLength, payloadHash, ex
        );
    }
}
