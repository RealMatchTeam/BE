package com.example.RealMatch.chat.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
    Optional<ChatRoom> findByRoomKey(String roomKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatRoom r
            SET r.lastMessageId = :messageId,
                r.lastMessageAt = :messageAt,
                r.lastMessagePreview = :messagePreview,
                r.lastMessageType = :messageType
            WHERE r.id = :roomId
              AND (r.lastMessageAt IS NULL OR r.lastMessageAt < :messageAt)
            """)
    int updateLastMessageIfNewer(
            @Param("roomId") Long roomId,
            @Param("messageId") Long messageId,
            @Param("messageAt") LocalDateTime messageAt,
            @Param("messagePreview") String messagePreview,
            @Param("messageType") ChatMessageType messageType
    );
}
