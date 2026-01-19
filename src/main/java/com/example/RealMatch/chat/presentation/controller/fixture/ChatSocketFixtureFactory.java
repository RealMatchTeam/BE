package com.example.RealMatch.chat.presentation.controller.fixture;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSendMessageAckStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSenderType;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentInfoResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

public final class ChatSocketFixtureFactory {

    private ChatSocketFixtureFactory() {
    }

    public static ChatMessageCreatedEvent sampleMessageCreatedEvent(ChatSendMessageCommand command) {
        ChatAttachmentInfoResponse attachment = sampleAttachment(command);
        String content = command.messageType() == ChatMessageType.TEXT ? command.content() : null;

        ChatMessageResponse message = new ChatMessageResponse(
                8001L,
                command.roomId(),
                202L,
                ChatSenderType.USER,
                command.messageType(),
                content,
                attachment,
                null,
                LocalDateTime.of(2025, 1, 1, 10, 2),
                command.clientMessageId()
        );
        return new ChatMessageCreatedEvent(command.roomId(), message);
    }

    public static ChatSendMessageAck sampleAck(ChatSendMessageCommand command, Long messageId) {
        return new ChatSendMessageAck(
                command.clientMessageId(),
                messageId,
                ChatSendMessageAckStatus.SUCCESS
        );
    }

    private static ChatAttachmentInfoResponse sampleAttachment(ChatSendMessageCommand command) {
        ChatMessageType messageType = command.messageType();
        if (messageType != ChatMessageType.IMAGE && messageType != ChatMessageType.FILE) {
            return null;
        }
        ChatAttachmentType attachmentType = messageType == ChatMessageType.IMAGE
                ? ChatAttachmentType.IMAGE
                : ChatAttachmentType.FILE;
        return new ChatAttachmentInfoResponse(
                command.attachmentId(),
                attachmentType,
                messageType == ChatMessageType.IMAGE ? "image/png" : "application/octet-stream",
                messageType == ChatMessageType.IMAGE ? "photo.png" : "file.bin",
                204800L,
                "https://cdn.example.com/attachments/8001",
                ChatAttachmentStatus.READY
        );
    }
}
