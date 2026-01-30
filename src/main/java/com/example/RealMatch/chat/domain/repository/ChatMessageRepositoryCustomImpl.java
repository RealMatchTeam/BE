package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    @Override
    public List<ChatMessage> findProposalMessagesByRoomId(Long roomId) {
        return queryFactory
                .selectFrom(MESSAGE)
                .where(
                        MESSAGE.roomId.eq(roomId),
                        MESSAGE.messageType.eq(ChatMessageType.SYSTEM),
                        MESSAGE.systemKind.in(
                                ChatSystemMessageKind.PROPOSAL_CARD,
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
        if (roomIds == null || roomIds.isEmpty() || !StringUtils.hasText(search)) {
            return Map.of();
        }
        String normalized = search.trim();
        List<ChatMessage> list = queryFactory
                .selectFrom(MESSAGE)
                .where(
                        MESSAGE.roomId.in(roomIds),
                        MESSAGE.content.isNotNull(),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.content.containsIgnoreCase(normalized)
                )
                .orderBy(MESSAGE.id.desc())
                .fetch();
        // 방별로 첫 번째(가장 최신) 메시지만 유지
        return list.stream()
                .collect(Collectors.toMap(ChatMessage::getRoomId, m -> m, (existing, replacement) -> existing));
    }
}
