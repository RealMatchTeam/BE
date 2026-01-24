package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatMessage;
import com.example.RealMatch.chat.domain.entity.QChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatProposalDirection;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
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
            ChatRoomTab tab,
            ChatRoomFilterStatus filterStatus,
            RoomCursorInfo cursorInfo,
            int size
    ) {
        return queryFactory
                .selectFrom(ROOM)
                .innerJoin(MEMBER).on(ROOM.id.eq(MEMBER.roomId))
                .where(
                        MEMBER.userId.eq(userId),
                        MEMBER.isDeleted.isFalse(),
                        ROOM.isDeleted.isFalse(),
                        ROOM.lastMessageAt.isNotNull(),
                        applyTabFilter(tab, ROOM, MEMBER),
                        applyStatusFilter(filterStatus, ROOM),
                        applyCursor(cursorInfo, ROOM)
                )
                .orderBy(
                        ROOM.lastMessageAt.desc(),
                        ROOM.id.desc()
                )
                .limit(size + 1)
                .fetch();
    }

    @Override
    public long countUnreadMessagesByUserAndTab(Long userId, ChatRoomTab tab) {
        QChatRoomMember memberForCount = new QChatRoomMember("memberForCount");
        QChatRoom roomForCount = new QChatRoom("roomForCount");

        Long count = queryFactory
                .select(MESSAGE.count())
                .from(MESSAGE)
                .innerJoin(memberForCount).on(
                        MESSAGE.roomId.eq(memberForCount.roomId)
                                .and(memberForCount.userId.eq(userId))
                                .and(memberForCount.isDeleted.isFalse())
                )
                .innerJoin(roomForCount).on(
                        MESSAGE.roomId.eq(roomForCount.id)
                                .and(roomForCount.isDeleted.isFalse())
                                .and(roomForCount.lastMessageAt.isNotNull())
                )
                .where(
                        applyTabFilter(tab, roomForCount, memberForCount),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.senderId.ne(userId),
                        isUnreadMessage(MESSAGE.id, memberForCount.lastReadMessageId)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Map<ChatRoomTab, Long> countUnreadMessagesByTabs(Long userId) {
        long sentCount = countUnreadMessagesByUserAndTab(userId, ChatRoomTab.SENT);
        long receivedCount = countUnreadMessagesByUserAndTab(userId, ChatRoomTab.RECEIVED);

        return Map.of(
                ChatRoomTab.SENT, sentCount,
                ChatRoomTab.RECEIVED, receivedCount
        );
    }

    @Override
    public Map<Long, Long> countUnreadMessagesByRoomIds(
            List<Long> roomIds,
            Long userId
    ) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }

        QChatRoomMember m = new QChatRoomMember("m");

        List<Tuple> results = queryFactory
                .select(MESSAGE.roomId, MESSAGE.count())
                .from(MESSAGE)
                .innerJoin(m).on(
                        MESSAGE.roomId.eq(m.roomId)
                                .and(m.userId.eq(userId))
                                .and(m.isDeleted.isFalse())
                )
                .where(
                        MESSAGE.roomId.in(roomIds),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.senderId.ne(userId),
                        isUnreadMessage(MESSAGE.id, m.lastReadMessageId)
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

    private BooleanExpression applyTabFilter(ChatRoomTab tab, QChatRoom r, QChatRoomMember m) {
        if (tab == null) {
            return null;
        }
        return switch (tab) {
            case SENT -> sentCondition(r, m);
            case RECEIVED -> receivedCondition(r, m);
        };
    }

    private BooleanExpression sentCondition(QChatRoom r, QChatRoomMember m) {
        return (r.lastProposalDirection.eq(ChatProposalDirection.BRAND_TO_CREATOR)
                .and(m.role.eq(ChatRoomMemberRole.BRAND)))
                .or(r.lastProposalDirection.eq(ChatProposalDirection.CREATOR_TO_BRAND)
                        .and(m.role.eq(ChatRoomMemberRole.CREATOR)));
    }

    private BooleanExpression receivedCondition(QChatRoom r, QChatRoomMember m) {
        return (r.lastProposalDirection.eq(ChatProposalDirection.BRAND_TO_CREATOR)
                .and(m.role.eq(ChatRoomMemberRole.CREATOR)))
                .or(r.lastProposalDirection.eq(ChatProposalDirection.CREATOR_TO_BRAND)
                        .and(m.role.eq(ChatRoomMemberRole.BRAND)));
    }

    private BooleanExpression applyStatusFilter(ChatRoomFilterStatus filterStatus, QChatRoom r) {
        if (filterStatus == null || filterStatus == ChatRoomFilterStatus.ALL) {
            return null;
        }
        ChatProposalStatus status = filterStatus.toProposalStatus();
        return r.proposalStatus.eq(status);
    }

    private BooleanExpression applyCursor(RoomCursorInfo cursorInfo, QChatRoom r) {
        if (cursorInfo == null) {
            return null;
        }
        return r.lastMessageAt.lt(cursorInfo.lastMessageAt())
                .or(r.lastMessageAt.eq(cursorInfo.lastMessageAt())
                        .and(r.id.lt(cursorInfo.roomId())));
    }
}
