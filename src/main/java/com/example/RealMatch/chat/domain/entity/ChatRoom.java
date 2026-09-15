package com.example.RealMatch.chat.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatRoomType;
import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@SQLRestriction("is_deleted = false")
@Table(
        name = "chat_room",
        indexes = {
                @Index(name = "idx_room_deleted_lastmsg", columnList = "is_deleted, last_message_at")
}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_key", nullable = false, unique = true, length = 128)
    private String roomKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_message_type", length = 20)
    private ChatMessageType lastMessageType;

    private ChatRoom(
            String roomKey,
            ChatRoomType roomType
    ) {
        this.roomKey = roomKey;
        this.roomType = roomType;
    }

    public static ChatRoom createDirectRoom(String roomKey) {
        return new ChatRoom(roomKey, ChatRoomType.DIRECT);
    }
}
