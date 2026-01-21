package com.example.RealMatch.chat.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatMessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_id")
    private Long attachmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_kind", length = 50)
    private ChatSystemMessageKind systemKind;

    @Column(name = "system_payload", columnDefinition = "json")
    private String systemPayload;

    @Column(name = "client_message_id", length = 36)
    private String clientMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private ChatMessage(
            Long roomId,
            Long senderId,
            ChatMessageType messageType,
            String content,
            Long attachmentId,
            String clientMessageId
    ) {
        validateInvariants(roomId, messageType, content, attachmentId);
        this.roomId = roomId;
        this.senderId = senderId;
        this.messageType = messageType;
        this.content = content;
        this.attachmentId = attachmentId;
        this.clientMessageId = clientMessageId;
    }

    private static void validateInvariants(
            Long roomId,
            ChatMessageType messageType,
            String content,
            Long attachmentId
    ) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room id must not be null.");
        }
        if (messageType == null) {
            throw new IllegalArgumentException("Message type must not be null.");
        }
        if (messageType == ChatMessageType.TEXT && (content == null || content.isBlank())) {
            throw new IllegalArgumentException("Content is required for TEXT messages.");
        }
        if ((messageType == ChatMessageType.IMAGE || messageType == ChatMessageType.FILE) && attachmentId == null) {
            throw new IllegalArgumentException("Attachment id is required for IMAGE/FILE messages.");
        }
    }

    public static ChatMessage createUserMessage(
            Long roomId,
            Long senderId,
            ChatMessageType messageType,
            String content,
            Long attachmentId,
            String clientMessageId
    ) {
        return new ChatMessage(roomId, senderId, messageType, content, attachmentId, clientMessageId);
    }

    public static ChatMessage createSystemMessage(
            Long roomId,
            ChatSystemMessageKind systemKind,
            String systemPayload
    ) {
        ChatMessage message = new ChatMessage(
                roomId,
                null,
                ChatMessageType.SYSTEM,
                null,
                null,
                null
        );
        message.systemKind = systemKind;
        message.systemPayload = systemPayload;
        return message;
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
