package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;

public interface ChatRoomMemberRepositoryCustom {
    Optional<ChatRoomMember> findActiveMemberByRoomIdAndUserId(Long roomId, Long userId);
    
    Optional<ChatRoomMember> findMemberByRoomIdAndUserIdWithRoomCheck(Long roomId, Long userId);
    
    List<ChatRoomMember> findActiveMembersByRoomId(Long roomId);
    
    List<ChatRoomMember> findActiveMembersByRoomIdIn(List<Long> roomIds);
}
