package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatMessage;
import com.example.RealMatch.chat.domain.entity.QChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.user.domain.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QChatRoom ROOM = QChatRoom.chatRoom;
    private static final QChatRoomMember MEMBER = QChatRoomMember.chatRoomMember;
    private static final QChatMessage MESSAGE = QChatMessage.chatMessage;

    @Override
    public List<ChatRoom> findRoomsByUser(
            Long userId,
            ChatRoomFilterStatus filterStatus,
            RoomCursorInfo cursorInfo,
            int size,
            String search
    ) {
        return queryFactory
                .selectFrom(ROOM)
                .innerJoin(MEMBER).on(ROOM.id.eq(MEMBER.roomId))
                .where(
                        MEMBER.userId.eq(userId),
                        MEMBER.isDeleted.isFalse(),
                        ROOM.isDeleted.isFalse(),
                        ROOM.lastMessageAt.isNotNull(),
                        applyFilterStatus(filterStatus, ROOM),
                        applyCursor(cursorInfo, ROOM),
                        applySearch(search, userId, ROOM)
                )
                .orderBy(
                        ROOM.lastMessageAt.desc(),
                        ROOM.id.desc()
                )
                .limit(size + 1)
                .fetch();
    }

    @Override
    public long countTotalUnreadMessages(Long userId) {
        // 공통 쿼리 로직 재사용
        Long count = queryFactory
                .select(MESSAGE.count())
                .from(MESSAGE)
                .innerJoin(MEMBER).on(
                        MESSAGE.roomId.eq(MEMBER.roomId)
                                .and(MEMBER.userId.eq(userId))
                                .and(MEMBER.isDeleted.isFalse())
                )
                .innerJoin(ROOM).on(
                        MESSAGE.roomId.eq(ROOM.id)
                                .and(ROOM.isDeleted.isFalse())
                                .and(ROOM.lastMessageAt.isNotNull())
                )
                .where(
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.senderId.ne(userId),
                        isUnreadMessage(MESSAGE.id, MEMBER.lastReadMessageId)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Map<Long, Long> countUnreadMessagesByRoomIds(
            List<Long> roomIds,
            Long userId
    ) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
                .select(MESSAGE.roomId, MESSAGE.count())
                .from(MESSAGE)
                .innerJoin(MEMBER).on(
                        MESSAGE.roomId.eq(MEMBER.roomId)
                                .and(MEMBER.userId.eq(userId))
                                .and(MEMBER.isDeleted.isFalse())
                )
                .innerJoin(ROOM).on(
                        MESSAGE.roomId.eq(ROOM.id)
                                .and(ROOM.isDeleted.isFalse())
                                .and(ROOM.lastMessageAt.isNotNull())
                )
                .where(
                        MESSAGE.roomId.in(roomIds),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.senderId.ne(userId),
                        isUnreadMessage(MESSAGE.id, MEMBER.lastReadMessageId)
                )
                .groupBy(MESSAGE.roomId)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(MESSAGE.roomId),
                        tuple -> tuple.get(MESSAGE.count())
                ));
    }

    private BooleanExpression isUnreadMessage(
            com.querydsl.core.types.dsl.NumberPath<Long> messageIdPath,
            com.querydsl.core.types.dsl.NumberPath<Long> lastReadMessageIdPath
    ) {
        return lastReadMessageIdPath.isNull()
                .or(messageIdPath.gt(lastReadMessageIdPath));
    }

    private BooleanExpression applyFilterStatus(ChatRoomFilterStatus filterStatus, QChatRoom r) {
        if (filterStatus == null || filterStatus == ChatRoomFilterStatus.LATEST) {
            return null; // 최신순은 필터링 없음
        }
        if (filterStatus == ChatRoomFilterStatus.COLLABORATING) {
            // 협업중: proposalStatus == MATCHED
            return r.proposalStatus.eq(ChatProposalStatus.MATCHED);
        }
        return null;
    }

    private BooleanExpression applyCursor(RoomCursorInfo cursorInfo, QChatRoom r) {
        if (cursorInfo == null) {
            return null;
        }
        return r.lastMessageAt.lt(cursorInfo.lastMessageAt())
                .or(r.lastMessageAt.eq(cursorInfo.lastMessageAt())
                        .and(r.id.lt(cursorInfo.roomId())));
    }

    private BooleanExpression applySearch(String search, Long userId, QChatRoom r) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        String normalized = search.trim();

        var messageRoomIds = JPAExpressions
                .select(MESSAGE.roomId)
                .from(MESSAGE)
                .where(
                        MESSAGE.content.isNotNull(),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.content.containsIgnoreCase(normalized)
                )
                .distinct();

        QChatRoomMember memberOpp = new QChatRoomMember("member_opp");
        QUser userOpp = new QUser("user_opp");
        var opponentRoomIds = JPAExpressions
                .select(memberOpp.roomId)
                .from(memberOpp)
                .innerJoin(userOpp).on(memberOpp.userId.eq(userOpp.id))
                .where(
                        memberOpp.userId.ne(userId),
                        memberOpp.isDeleted.isFalse(),
                        userOpp.isDeleted.isFalse(),
                        userOpp.nickname.containsIgnoreCase(normalized)
                )
                .distinct();

        return r.id.in(messageRoomIds).or(r.id.in(opponentRoomIds));
    }
}
