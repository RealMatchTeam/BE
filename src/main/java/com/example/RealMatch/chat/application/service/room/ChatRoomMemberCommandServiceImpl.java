package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.cache.ChatCacheInvalidationService;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberCommandServiceImpl implements ChatRoomMemberCommandService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatCacheInvalidationService cacheInvalidationService;
    private final AfterCommitExecutor afterCommitExecutor;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastReadMessage(@NonNull Long memberId, @NonNull Long messageId) {
        ChatRoomMember member = chatRoomMemberRepository.findById(memberId)
                .orElse(null);
        if (member == null) {
            return;
        }
        Long currentLastRead = member.getLastReadMessageId();
        if (currentLastRead == null || currentLastRead < messageId) {
            member.updateLastReadMessage(messageId, LocalDateTime.now());
            afterCommitExecutor.execute(() -> cacheInvalidationService.invalidateAfterMemberRead(member.getUserId()));
        }
    }
}
