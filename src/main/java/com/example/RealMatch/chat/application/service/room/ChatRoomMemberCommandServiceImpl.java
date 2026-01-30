package com.example.RealMatch.chat.application.service.room;

import java.time.LocalDateTime;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberCommandServiceImpl implements ChatRoomMemberCommandService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastReadMessage(@NonNull Long memberId, @NonNull Long messageId) {
        ChatRoomMember member = chatRoomMemberRepository.findById(memberId)
                .orElse(null);
        if (member != null && (member.getLastReadMessageId() == null || member.getLastReadMessageId() < messageId)) {
            member.updateLastReadMessage(messageId, LocalDateTime.now());
        }
    }
}
