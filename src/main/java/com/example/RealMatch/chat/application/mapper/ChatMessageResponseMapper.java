package com.example.RealMatch.chat.application.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.RealMatch.attachment.presentation.dto.response.AttachmentInfoResponse;
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
    private static final int SYSTEM_MESSAGE_SCHEMA_VERSION = 1;

    private final SystemMessagePayloadSerializer payloadSerializer;

    public ChatMessageResponse toResponse(ChatMessage message, AttachmentInfoResponse attachment) {
        if (message == null) {
            throw new IllegalStateException("Message must not be null when mapping to response.");
        }
        
        Long messageId = message.getId();
        if (messageId == null) {
            throw new IllegalStateException(
                    "Message must have id when mapping to response. This indicates a save() contract violation.");
        }
        
        ChatMessageType messageType = message.getMessageType();
        if (messageType == null) {
            throw new IllegalStateException(
                    String.format("Message type must not be null. messageId=%d", messageId));
        }
        
        return new ChatMessageResponse(
                messageId,
                message.getRoomId(),
                message.getSenderId(),
                messageType == ChatMessageType.SYSTEM ? ChatSenderType.SYSTEM : ChatSenderType.USER,
                messageType,
                message.getContent(),
                attachment,
                messageType == ChatMessageType.SYSTEM ? toSystemMessageResponse(message) : null,
                message.getCreatedAt(),
                message.getClientMessageId()
        );
    }

    private ChatSystemMessageResponse toSystemMessageResponse(ChatMessage message) {
        Long messageId = message.getId();
        ChatSystemMessageKind kind = message.getSystemKind();
        String rawPayload = message.getSystemPayload();
        
        // SYSTEM 메시지는 반드시 kind와 payload가 있어야 함
        if (kind == null) {
            throw new IllegalStateException(
                    String.format("SYSTEM message kind must not be null. messageId=%d", messageId));
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalStateException(
                    String.format("SYSTEM message payload must not be null or blank. messageId=%d, kind=%s", 
                            messageId, kind));
        }
        
        try {
            ChatSystemMessagePayload payload = payloadSerializer.deserialize(kind, rawPayload);
            return new ChatSystemMessageResponse(SYSTEM_MESSAGE_SCHEMA_VERSION, kind, payload);
        } catch (IllegalArgumentException ex) {
            int payloadLength = rawPayload.length();
            String payloadHash = Integer.toHexString(rawPayload.hashCode());
            LOG.error("Failed to deserialize system message payload. messageId={}, kind={}, payloadLength={}, payloadHash={}", 
                    messageId, kind, payloadLength, payloadHash, ex);
            throw new IllegalStateException(
                    String.format("Failed to deserialize system message payload. messageId=%d, kind=%s, payloadLength=%d", 
                            messageId, kind, payloadLength), ex);
        } catch (RuntimeException ex) {
            // serializer가 예상치 못한 RuntimeException을 던질 경우
            int payloadLength = rawPayload != null ? rawPayload.length() : 0;
            String payloadHash = rawPayload != null ? Integer.toHexString(rawPayload.hashCode()) : "null";
            LOG.error("Unexpected exception during system message payload deserialization. messageId={}, kind={}, payloadLength={}, payloadHash={}", 
                    messageId, kind, payloadLength, payloadHash, ex);
            throw new IllegalStateException(
                    String.format("Failed to deserialize system message payload. messageId=%d, kind=%s", 
                            messageId, kind), ex);
        }
    }
}
