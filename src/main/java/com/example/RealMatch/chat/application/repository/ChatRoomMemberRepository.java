package com.example.RealMatch.chat.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;

public interface ChatRoomMemberRepository extends ChatRoomMemberRepositoryCustom {
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    List<ChatRoomMember> findByUserIdAndRoomIdIn(Long userId, List<Long> roomIds);

    List<ChatRoomMember> findByRoomIdIn(List<Long> roomIds);

    List<ChatRoomMember> findByRoomId(Long roomId);

    int updateLastReadMessageIfNewer(
             Long memberId,
             Long messageId,
             LocalDateTime readAt
    );
    <S extends ChatRoomMember> S saveAndFlush(S entity);
}
