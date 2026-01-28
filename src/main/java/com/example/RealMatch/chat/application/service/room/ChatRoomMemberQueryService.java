package com.example.RealMatch.chat.application.service.room;

import java.util.List;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;

public interface ChatRoomMemberQueryService {

    List<ChatRoomMember> findActiveMembers(Long roomId);
}
