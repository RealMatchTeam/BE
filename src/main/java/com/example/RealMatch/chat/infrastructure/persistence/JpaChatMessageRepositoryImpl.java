package com.example.RealMatch.chat.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.chat.application.repository.ChatMessageRepositoryCustom;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.QChatMessage;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QChatMessage MESSAGE = QChatMessage.chatMessage;

    @Override
    public Optional<ChatMessage> findLatestProposalCardMessageByRoomId(Long roomId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(MESSAGE)
                        .where(
                                MESSAGE.roomId.eq(roomId),
                                MESSAGE.messageType.eq(ChatMessageType.SYSTEM),
                                MESSAGE.systemKind.in(
                                        ChatSystemMessageKind.PROPOSAL_CARD,
                                        ChatSystemMessageKind.RE_PROPOSAL_CARD
                                )
                        )
                        .orderBy(MESSAGE.id.desc())
                        .limit(1)
                        .fetchOne()
        );
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
        QChatMessage match = new QChatMessage("matchingMessage");
        List<ChatMessage> list = queryFactory
                .selectFrom(MESSAGE)
                .where(
                        MESSAGE.id.in(JPAExpressions.select(match.id.max()).from(match)
                                .where(match.roomId.in(roomIds), match.senderId.isNotNull(),
                                        match.content.containsIgnoreCase(normalized))
                                .groupBy(match.roomId))
                )
                .fetch();
        // 방별로 첫 번째(가장 최신) 메시지만 유지
        return list.stream()
                .collect(Collectors.toMap(ChatMessage::getRoomId, m -> m, (existing, replacement) -> existing));
    }
}
