package com.example.RealMatch.chat.application.service.message;

import com.example.RealMatch.chat.domain.entity.ChatAttachment;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSenderType;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentInfoResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessageResponse;

public class ChatMessageResponseMapper {

    private static final int SYSTEM_MESSAGE_SCHEMA_VERSION = 1;

    private final SystemMessagePayloadSerializer payloadSerializer;

    public ChatMessageResponseMapper(SystemMessagePayloadSerializer payloadSerializer) {
        this.payloadSerializer = payloadSerializer;
    }

    public ChatMessageResponse toResponse(ChatMessage message, ChatAttachment attachment) {
        ChatMessageType messageType = message.getMessageType();
        return new ChatMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                messageType == ChatMessageType.SYSTEM ? ChatSenderType.SYSTEM : ChatSenderType.USER,
                messageType,
                message.getContent(),
                toAttachmentResponse(attachment),
                messageType == ChatMessageType.SYSTEM ? toSystemMessageResponse(message) : null,
                message.getCreatedAt(),
                message.getClientMessageId()
        );
    }

    private ChatSystemMessageResponse toSystemMessageResponse(ChatMessage message) {
        ChatSystemMessageKind kind = message.getSystemKind();
        ChatSystemMessagePayload payload = payloadSerializer.deserialize(kind, message.getSystemPayload());
        return new ChatSystemMessageResponse(SYSTEM_MESSAGE_SCHEMA_VERSION, kind, payload);
    }

    private ChatAttachmentInfoResponse toAttachmentResponse(ChatAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new ChatAttachmentInfoResponse(
                attachment.getId(),
                attachment.getAttachmentType(),
                attachment.getContentType(),
                attachment.getOriginalName(),
                attachment.getFileSize(),
                attachment.getAccessUrl(),
                attachment.getStatus()
        );
    }
}
