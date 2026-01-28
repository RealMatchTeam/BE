package com.example.RealMatch.chat.domain.repository;

import java.util.List;

import com.example.RealMatch.chat.domain.entity.ChatMessage;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findProposalMessagesByRoomId(Long roomId);

    List<ChatMessage> findMessagesByRoomId(Long roomId, Long cursorMessageId, int size);
}
