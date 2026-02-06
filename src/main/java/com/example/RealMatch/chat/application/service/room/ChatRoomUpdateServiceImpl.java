package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.cache.ChatCacheInvalidationService;
import com.example.RealMatch.chat.application.exception.ChatRoomNotFoundException;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.application.util.ChatRoomKeyGenerator;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomUpdateServiceImpl implements ChatRoomUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomUpdateServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatCacheInvalidationService cacheInvalidationService;

    @Override
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

    @Override
    @Transactional
    public void updateProposalStatusByUsers(
            @NonNull Long brandUserId,
            @NonNull Long creatorUserId,
            @NonNull ChatProposalStatus status
    ) {
        String roomKey = ChatRoomKeyGenerator.createDirectRoomKey(brandUserId, creatorUserId);
        ChatRoom room = chatRoomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> {
                    String message = String.format(
                            "Chat room not found for proposal status update. brandUserId=%d, creatorUserId=%d, status=%s",
                            brandUserId, creatorUserId, status
                    );
                    LOG.warn("[ChatRoomUpdate] {}. This may indicate a data inconsistency.", message);
                    return new ChatRoomNotFoundException(message);
                });

        room.updateProposalStatus(status);
        LOG.debug("[ChatRoomUpdate] Chat room proposal status updated. roomId={}, status={}", room.getId(), status);

        afterCommitExecutor.execute(() -> {
            cacheInvalidationService.invalidateAfterProposalStatusChanged(
                    room.getId(), brandUserId, creatorUserId
            );
        });
    }
}
