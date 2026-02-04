package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.QChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryCustomImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QChatMessage MESSAGE = QChatMessage.chatMessage;
    private static final int SEARCH_RESULT_LIMIT_PER_ROOM = 50;
    private static final long SEARCH_RESULT_MAX_LIMIT = 2000;

    @Override
    public List<ChatMessage> findProposalMessagesByRoomId(Long roomId) {
        return queryFactory
                .selectFrom(MESSAGE)
                .where(
                        MESSAGE.roomId.eq(roomId),
                        MESSAGE.messageType.eq(ChatMessageType.SYSTEM),
                        MESSAGE.systemKind.in(
                                ChatSystemMessageKind.PROPOSAL_CARD,
                                ChatSystemMessageKind.RE_PROPOSAL_CARD,
                                ChatSystemMessageKind.MATCHED_CAMPAIGN_CARD
                        )
                )
                .orderBy(MESSAGE.id.desc())
                .fetch();
    }

    @Override
    public List<ChatMessage> findMessagesByRoomId(Long roomId, Long cursorMessageId, int size) {
        var query = queryFactory
                .selectFrom(MESSAGE)
                .where(MESSAGE.roomId.eq(roomId));

        if (cursorMessageId != null) {
            query = query.where(MESSAGE.id.lt(cursorMessageId));
        }

        return query
                .orderBy(MESSAGE.id.desc())
                .limit(size + 1)
                .fetch();
    }

    @Override
    public Map<Long, ChatMessage> findLatestMatchingMessageByRoomIds(List<Long> roomIds, String search) {
        if (roomIds == null || roomIds.isEmpty() || search == null || search.isBlank()) {
            return Map.of();
        }
        String normalized = search.trim();
        long limit = Math.min(
                (long) roomIds.size() * SEARCH_RESULT_LIMIT_PER_ROOM,
                SEARCH_RESULT_MAX_LIMIT
        );
        List<ChatMessage> list = queryFactory
                .selectFrom(MESSAGE)
                .where(
                        MESSAGE.roomId.in(roomIds),
                        MESSAGE.content.isNotNull(),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.content.containsIgnoreCase(normalized)
                )
                .orderBy(MESSAGE.id.desc())
                .limit(limit)
                .fetch();
        // 방별로 첫 번째(가장 최신) 메시지만 유지
        return list.stream()
                .collect(Collectors.toMap(ChatMessage::getRoomId, m -> m, (existing, replacement) -> existing));
    }
}
