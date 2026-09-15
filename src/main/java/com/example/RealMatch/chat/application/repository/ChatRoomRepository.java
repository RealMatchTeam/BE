package com.example.RealMatch.chat.application.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;

public interface ChatRoomRepository extends ChatRoomRepositoryCustom {
    Optional<ChatRoom> findByRoomKey(String roomKey);

    Optional<ChatRoom> findByIdForUpdate(Long id);

    Optional<ChatRoom> findByRoomKeyForUpdate(String roomKey);

    int updateLastMessageIfNewer(
             Long roomId,
             Long messageId,
             LocalDateTime messageAt,
             String messagePreview,
             ChatMessageType messageType
    );
    <S extends ChatRoom> S saveAndFlush(S entity);
    Optional<ChatRoom> findById(Long id);
    boolean existsById(Long id);
}
