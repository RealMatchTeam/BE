package com.example.RealMatch.chat.domain.entity;

import java.time.LocalDateTime;

import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.global.common.DeleteBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "chat_room_member",
        indexes = {
                @Index(name = "idx_member_user_deleted_room", columnList = "user_id, is_deleted, room_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_member_room_user",
                        columnNames = {"room_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends DeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ChatRoomMemberRole role;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_muted", nullable = false)
    private Boolean isMuted;

    private ChatRoomMember(
            Long roomId,
            Long userId,
            ChatRoomMemberRole role
    ) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.isMuted = false;
    }

    public static ChatRoomMember create(Long roomId, Long userId, ChatRoomMemberRole role) {
        return new ChatRoomMember(roomId, userId, role);
    }

    @PrePersist
    private void prePersist() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public void updateLastReadMessage(Long messageId, LocalDateTime readAt) {
        this.lastReadMessageId = messageId;
        this.lastReadAt = readAt;
    }
}
