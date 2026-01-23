package com.example.RealMatch.chat.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.chat.domain.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findByClientMessageIdAndSenderId(String clientMessageId, Long senderId);

    long countByRoomIdAndIdGreaterThan(Long roomId, Long messageId);

    long countByRoomIdAndIdGreaterThanAndSenderIdNot(Long roomId, Long messageId, Long senderId);
}
