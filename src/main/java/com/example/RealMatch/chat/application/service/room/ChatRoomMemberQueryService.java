package com.example.RealMatch.chat.application.service.room;

import java.util.List;

public interface ChatRoomMemberQueryService {

    List<Long> findActiveMemberUserIds(Long roomId);
}
