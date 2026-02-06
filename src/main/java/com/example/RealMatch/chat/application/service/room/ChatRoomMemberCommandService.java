package com.example.RealMatch.chat.application.service.room;

import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRoomMemberCommandService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateLastReadMessage(@NonNull Long memberId, @NonNull Long userId, @NonNull Long messageId);
}
