package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.repository.ChatRoomRepository;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomUpdateService.class);

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void updateLastMessage(
            @NonNull Long roomId,
            @NonNull Long messageId,
            LocalDateTime messageAt,
            String messagePreview,
            ChatMessageType messageType
    ) {
        LocalDateTime safeMessageAt = messageAt != null ? messageAt : LocalDateTime.now();

        int updatedRows = chatRoomRepository.updateLastMessageIfNewer(
                roomId, messageId, safeMessageAt, messagePreview, messageType
        );

        if (updatedRows == 0 && !chatRoomRepository.existsById(roomId)) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        if (updatedRows > 1) {
            LOG.warn("[ChatRoomUpdate] Unexpected update count. roomId={}, messageId={}, rows={}",
                    roomId, messageId, updatedRows);
        }
    }
}
