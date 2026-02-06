package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.cache.ChatCacheInvalidationService;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberCommandServiceImpl implements ChatRoomMemberCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatRoomMemberCommandServiceImpl.class);

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatCacheInvalidationService cacheInvalidationService;
    private final AfterCommitExecutor afterCommitExecutor;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastReadMessage(@NonNull Long memberId, @NonNull Long userId, @NonNull Long messageId) {
        int updatedRows = chatRoomMemberRepository.updateLastReadMessageIfNewer(
                memberId, messageId, LocalDateTime.now()
        );

        if (updatedRows == 1) {
            afterCommitExecutor.execute(() -> cacheInvalidationService.invalidateAfterMemberRead(userId));
        } else if (updatedRows > 1) {
            LOG.warn("[ChatRoomMember] Unexpected update count. memberId={}, messageId={}, rows={}",
                    memberId, messageId, updatedRows);
        }
    }
}
