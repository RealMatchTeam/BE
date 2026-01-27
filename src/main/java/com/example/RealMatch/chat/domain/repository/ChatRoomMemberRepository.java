package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long>, ChatRoomMemberRepositoryCustom {
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    List<ChatRoomMember> findByUserIdAndRoomIdIn(Long userId, List<Long> roomIds);

    List<ChatRoomMember> findByRoomIdIn(List<Long> roomIds);

    List<ChatRoomMember> findByRoomId(Long roomId);
}
