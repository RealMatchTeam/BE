package com.example.RealMatch.chat.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.example.RealMatch.chat.domain.entity.ChatMessage;

import jakarta.persistence.LockModeType;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
    Optional<ChatMessage> findByClientMessageIdAndSenderId(String clientMessageId, Long senderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatMessage> findByRoomIdAndSystemEventId(Long roomId, String systemEventId);

    long countByRoomIdAndIdGreaterThan(Long roomId, Long messageId);

    long countByRoomIdAndIdGreaterThanAndSenderIdNot(Long roomId, Long messageId, Long senderId);
}
