package com.example.RealMatch.chat.application.service.room;

import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;

public interface ChatRoomCommandService {

    /**
     * 사용자 요청 시 사용. 요청자(userId)가 brandId 또는 creatorId일 때만 허용
     */
    ChatRoomCreateResponse createOrGetRoomAsMember(Long userId, Long brandId, Long creatorId);

    /**
     * 이벤트/시스템에서 사용. 권한 검증 없음.
     */
    ChatRoomCreateResponse createOrGetRoomSystem(Long brandId, Long creatorId);
}
