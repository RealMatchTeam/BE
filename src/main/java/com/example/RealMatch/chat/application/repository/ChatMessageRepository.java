package com.example.RealMatch.chat.application.repository;

import java.util.Optional;

import com.example.RealMatch.chat.domain.entity.ChatMessage;

public interface ChatMessageRepository extends ChatMessageRepositoryCustom {
    Optional<ChatMessage> findByClientMessageIdAndSenderId(String clientMessageId, Long senderId);

    Optional<ChatMessage> findByRoomIdAndSystemEventId(Long roomId, String systemEventId);

    boolean existsByIdAndRoomId(Long id, Long roomId);

    long countByRoomIdAndIdGreaterThan(Long roomId, Long messageId);

    long countByRoomIdAndIdGreaterThanAndSenderIdNot(Long roomId, Long messageId, Long senderId);
    <S extends ChatMessage> S saveAndFlush(S entity);
}
