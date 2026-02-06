package com.example.RealMatch.chat.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long>, ChatRoomMemberRepositoryCustom {
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    List<ChatRoomMember> findByUserIdAndRoomIdIn(Long userId, List<Long> roomIds);

    List<ChatRoomMember> findByRoomIdIn(List<Long> roomIds);

    List<ChatRoomMember> findByRoomId(Long roomId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ChatRoomMember m
            SET m.lastReadMessageId = :messageId,
                m.lastReadAt = :readAt
            WHERE m.id = :memberId
              AND (m.lastReadMessageId IS NULL OR m.lastReadMessageId < :messageId)
            """)
    int updateLastReadMessageIfNewer(
            @Param("memberId") Long memberId,
            @Param("messageId") Long messageId,
            @Param("readAt") LocalDateTime readAt
    );
}
