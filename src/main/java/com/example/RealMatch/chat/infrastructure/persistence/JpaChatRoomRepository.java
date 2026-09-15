package com.example.RealMatch.chat.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.chat.application.repository.ChatRoomRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomRepositoryCustom;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;

import jakarta.persistence.LockModeType;

public interface JpaChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepository, ChatRoomRepositoryCustom {
    Optional<ChatRoom> findByRoomKey(String roomKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.id = :id")
    Optional<ChatRoom> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.roomKey = :roomKey")
    Optional<ChatRoom> findByRoomKeyForUpdate(@Param("roomKey") String roomKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatRoom r
            SET r.lastMessageId = :messageId,
                r.lastMessageAt = :messageAt,
                r.lastMessagePreview = :messagePreview,
                r.lastMessageType = :messageType
            WHERE r.id = :roomId
              AND (r.lastMessageId IS NULL OR r.lastMessageId < :messageId)
            """)
    int updateLastMessageIfNewer(
            @Param("roomId") Long roomId,
            @Param("messageId") Long messageId,
            @Param("messageAt") LocalDateTime messageAt,
            @Param("messagePreview") String messagePreview,
            @Param("messageType") ChatMessageType messageType
    );
    @Override
    <S extends ChatRoom> S saveAndFlush(S entity);
    @Override
    Optional<ChatRoom> findById(Long id);
    @Override
    boolean existsById(Long id);
}
