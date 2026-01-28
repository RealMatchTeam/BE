package com.example.RealMatch.chat.application.service.room;

import java.util.List;
import java.util.Map;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;

public interface OpponentInfoService {

    OpponentInfo getOpponentInfo(Long opponentUserId);

    Map<Long, OpponentInfo> getOpponentInfoMapBatch(Long userId, List<Long> roomIds);

    ChatRoomMember getOpponentMember(Long roomId, Long userId);

    record OpponentInfo(Long userId, String name, String profileImageUrl) {
    }
}
