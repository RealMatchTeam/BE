package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.repository.ChatMessageRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomReadService {

    private final ChatRoomMemberRepository members;
    private final ChatRoomMemberService membership;
    private final ChatMessageRepository messages;
    private final ChatMessageEventPublisher publisher;
    private final AfterCommitExecutor afterCommit;

    @Transactional
    public void markRead(Long roomId, Long userId, Long messageId) {
        var member = membership.getActiveMemberOrThrow(roomId, userId);
        if (messageId == null || !messages.existsByIdAndRoomId(messageId, roomId)) {
            throw new CustomException(ChatErrorCode.INVALID_ROOM_FOR_MESSAGE);
        }
        if (members.updateLastReadMessageIfNewer(
                member.getId(), messageId, LocalDateTime.now()) == 1) {
            afterCommit.execute(() -> publisher.publishRoomListUpdated(roomId));
        }
    }
}
