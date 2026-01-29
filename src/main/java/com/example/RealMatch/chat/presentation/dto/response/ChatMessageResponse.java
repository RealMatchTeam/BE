package com.example.RealMatch.chat.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.RealMatch.attachment.presentation.dto.response.AttachmentInfoResponse;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSenderType;

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
