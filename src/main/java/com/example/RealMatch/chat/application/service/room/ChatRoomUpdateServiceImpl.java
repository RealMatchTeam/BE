package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;

@Service
public class ChatRoomUpdateServiceImpl implements ChatRoomUpdateService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomUpdateServiceImpl(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Override
    @Transactional
    public void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            ChatMessageType messageType,
            String messagePreview
    ) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        room.updateLastMessage(messageId, messageAt, messagePreview, messageType);
        chatRoomRepository.save(room);
    }
}
