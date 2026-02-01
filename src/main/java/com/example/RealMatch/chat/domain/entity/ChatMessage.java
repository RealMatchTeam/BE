package com.example.RealMatch.chat.domain.entity;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "chat_message",
        indexes = {
                @Index(name = "idx_message_room_sender_id", columnList = "room_id, sender_id, id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_message_sender_client",
                        columnNames = {"sender_id", "client_message_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

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

    private ChatMessage(
            Long roomId,
            ChatSystemMessageKind systemKind,
            String systemPayload
    ) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room id must not be null.");
        }
        validateSystemMessageInvariants(systemKind, systemPayload);
        this.roomId = roomId;
        this.senderId = null;
        this.messageType = ChatMessageType.SYSTEM;
        this.content = null;
        this.attachmentId = null;
        this.clientMessageId = null;
        this.systemKind = systemKind;
        this.systemPayload = systemPayload;
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
        if (messageType == ChatMessageType.SYSTEM) {
            throw new IllegalArgumentException("Use createSystemMessage for SYSTEM messages.");
        }
        if (messageType == ChatMessageType.TEXT && (content == null || content.isBlank())) {
            throw new IllegalArgumentException("Content is required for TEXT messages.");
        }
        if ((messageType == ChatMessageType.IMAGE || messageType == ChatMessageType.FILE) && attachmentId == null) {
            throw new IllegalArgumentException("Attachment id is required for IMAGE/FILE messages.");
        }
    }

    private static void validateSystemMessageInvariants(
            ChatSystemMessageKind systemKind,
            String systemPayload
    ) {
        if (systemKind == null) {
            throw new IllegalArgumentException("System message kind must not be null.");
        }
        if (systemKind.isPayloadRequired() && (systemPayload == null || systemPayload.isBlank())) {
            throw new IllegalArgumentException(
                    "System message payload is required for kind: " + systemKind.name());
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
        return new ChatMessage(roomId, systemKind, systemPayload);
    }
}
