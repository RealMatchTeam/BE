package com.example.RealMatch.chat.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.example.RealMatch.chat.application.repository.ChatMessageRepository;
import com.example.RealMatch.chat.application.repository.ChatMessageRepositoryCustom;
import com.example.RealMatch.chat.domain.entity.ChatMessage;

import jakarta.persistence.LockModeType;

public interface JpaChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepository, ChatMessageRepositoryCustom {
    // Call only after locking the room: current read avoids an older MySQL RR snapshot.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatMessage> findByClientMessageIdAndSenderId(String clientMessageId, Long senderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatMessage> findByRoomIdAndSystemEventId(Long roomId, String systemEventId);

    boolean existsByIdAndRoomId(Long id, Long roomId);

    long countByRoomIdAndIdGreaterThan(Long roomId, Long messageId);

    long countByRoomIdAndIdGreaterThanAndSenderIdNot(Long roomId, Long messageId, Long senderId);
    @Override
    <S extends ChatMessage> S saveAndFlush(S entity);
}
