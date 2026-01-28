package com.example.RealMatch.chat.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.entity.QChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatRoomMember;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomMemberRepositoryCustomImpl implements ChatRoomMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QChatRoomMember MEMBER = QChatRoomMember.chatRoomMember;
    private static final QChatRoom ROOM = QChatRoom.chatRoom;

    @Override
    public Optional<ChatRoomMember> findActiveMemberByRoomIdAndUserId(Long roomId, Long userId) {
        ChatRoomMember member = queryFactory
                .selectFrom(MEMBER)
                .innerJoin(ROOM).on(MEMBER.roomId.eq(ROOM.id))
                .where(
                        MEMBER.roomId.eq(roomId),
                        MEMBER.userId.eq(userId),
                        MEMBER.isDeleted.isFalse(),
                        MEMBER.leftAt.isNull(),
                        ROOM.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(member);
    }

    @Override
    public Optional<ChatRoomMember> findMemberByRoomIdAndUserIdWithRoomCheck(Long roomId, Long userId) {
        ChatRoomMember member = queryFactory
                .selectFrom(MEMBER)
                .innerJoin(ROOM).on(MEMBER.roomId.eq(ROOM.id))
                .where(
                        MEMBER.roomId.eq(roomId),
                        MEMBER.userId.eq(userId),
                        MEMBER.isDeleted.isFalse(),
                        ROOM.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(member);
    }

    @Override
    public List<ChatRoomMember> findActiveMembersByRoomId(Long roomId) {
        return queryFactory
                .selectFrom(MEMBER)
                .innerJoin(ROOM).on(MEMBER.roomId.eq(ROOM.id))
                .where(
                        MEMBER.roomId.eq(roomId),
                        MEMBER.isDeleted.isFalse(),
                        MEMBER.leftAt.isNull(),
                        ROOM.isDeleted.isFalse()
                )
                .fetch();
    }

    @Override
    public List<ChatRoomMember> findActiveMembersByRoomIdIn(List<Long> roomIds) {
        return queryFactory
                .selectFrom(MEMBER)
                .innerJoin(ROOM).on(MEMBER.roomId.eq(ROOM.id))
                .where(
                        MEMBER.roomId.in(roomIds),
                        MEMBER.isDeleted.isFalse(),
                        MEMBER.leftAt.isNull(),
                        ROOM.isDeleted.isFalse()
                )
                .fetch();
    }
}
