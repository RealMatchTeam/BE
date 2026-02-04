package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.RealMatch.chat.domain.entity.ChatMessage;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findProposalMessagesByRoomId(Long roomId);

    Optional<ChatMessage> findLatestProposalCardMessageByRoomId(Long roomId);

    List<ChatMessage> findMessagesByRoomId(Long roomId, Long cursorMessageId, int size);

    Map<Long, ChatMessage> findLatestMatchingMessageByRoomIds(List<Long> roomIds, String search);
}
