package com.example.RealMatch.chat.application.cache;

import java.util.EnumSet;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatCacheInvalidationService {

    private static final EnumSet<ChatSystemMessageKind> DETAIL_INVALIDATION_KINDS =
            EnumSet.of(
                    ChatSystemMessageKind.PROPOSAL_CARD,
                    ChatSystemMessageKind.RE_PROPOSAL_CARD,
                    ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE,
                    ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD
            );

    private final ChatCacheEvictor chatCacheEvictor;

    public void invalidateAfterMessageSaved(Long roomId, ChatSystemMessageKind kind) {
        if (roomId == null) {
            return;
        }
        chatCacheEvictor.evictRoomListByRoom(roomId);
        if (kind != null && DETAIL_INVALIDATION_KINDS.contains(kind)) {
            chatCacheEvictor.evictRoomDetailByRoom(roomId);
        }
    }

    public void invalidateAfterRoomCreated(Long brandUserId, Long creatorUserId) {
        chatCacheEvictor.evictRoomListByUser(brandUserId);
        chatCacheEvictor.evictRoomListByUser(creatorUserId);
    }

    public void invalidateAfterProposalStatusChanged(Long roomId, Long brandUserId, Long creatorUserId) {
        if (roomId != null) {
            chatCacheEvictor.evictRoomDetailByRoom(roomId);
        }
        chatCacheEvictor.evictRoomListByUser(brandUserId);
        chatCacheEvictor.evictRoomListByUser(creatorUserId);
    }

    public void invalidateAfterMemberRead(Long userId) {
        chatCacheEvictor.evictRoomListByUser(userId);
    }
}
