package com.example.RealMatch.chat.application.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.attachment.application.dto.response.AttachmentInfoResponse;
import com.example.RealMatch.chat.application.dto.enums.ChatSenderType;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;

public record ChatMessageResponse(
        Long messageId,
        Long roomId,
        Long senderId,
        ChatSenderType senderType,
        ChatMessageType messageType,
        String content,
        AttachmentInfoResponse attachment,
        ChatSystemMessageResponse systemMessage,
        LocalDateTime createdAt,
        String clientMessageId
) {
}
