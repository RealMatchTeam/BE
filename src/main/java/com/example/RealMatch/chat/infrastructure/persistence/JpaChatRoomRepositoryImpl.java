package com.example.RealMatch.chat.infrastructure.persistence;

import static com.example.RealMatch.business.domain.enums.ProposalStatus.MATCHED;
import static com.example.RealMatch.campaign.domain.enums.CampaignStatus.COMPLETED;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.RealMatch.business.domain.entity.QCampaignProposal;
import com.example.RealMatch.campaign.domain.enums.CampaignStatus;
import com.example.RealMatch.chat.application.repository.ChatRoomRepositoryCustom;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatMessage;
import com.example.RealMatch.chat.domain.entity.QChatRoom;
import com.example.RealMatch.chat.domain.entity.QChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.user.domain.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

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
                        MEMBER.isDeleted.isFalse().and(MEMBER.leftAt.isNull()),
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
        return countUnreadMessagesByUser(userId).values().stream().mapToLong(Long::longValue).sum();
    }

    @Override
    public Map<Long, Long> countUnreadMessagesByUser(Long userId) {
        List<Tuple> results = queryFactory
                .select(MESSAGE.roomId, MESSAGE.count())
                .from(MESSAGE)
                .innerJoin(MEMBER).on(
                        MESSAGE.roomId.eq(MEMBER.roomId)
                                .and(MEMBER.userId.eq(userId))
                                .and(MEMBER.isDeleted.isFalse().and(MEMBER.leftAt.isNull()))
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
                .groupBy(MESSAGE.roomId)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(MESSAGE.roomId),
                        tuple -> tuple.get(MESSAGE.count())
                ));
    }

    private BooleanExpression isUnreadMessage(
            NumberPath<Long> messageIdPath,
            NumberPath<Long> lastReadMessageIdPath
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
            return collaborating(r);
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

        var matchingMessage = JPAExpressions
                .selectOne()
                .from(MESSAGE)
                .where(
                        MESSAGE.roomId.eq(r.id),
                        MESSAGE.content.isNotNull(),
                        MESSAGE.senderId.isNotNull(),
                        MESSAGE.content.containsIgnoreCase(normalized)
                )
                .exists();

        QChatRoomMember memberOpp = new QChatRoomMember("member_opp");
        QUser userOpp = new QUser("user_opp");
        var matchingOpponent = JPAExpressions
                .selectOne()
                .from(memberOpp)
                .innerJoin(userOpp).on(memberOpp.userId.eq(userOpp.id))
                .where(
                        memberOpp.roomId.eq(r.id),
                        memberOpp.userId.ne(userId),
                        memberOpp.isDeleted.isFalse().and(memberOpp.leftAt.isNull()),
                        userOpp.isDeleted.isFalse(),
                        userOpp.nickname.containsIgnoreCase(normalized)
                )
                .exists();

        // Search only within an authorized room. Preserve substring matching, including short Korean terms.
        return matchingMessage.or(matchingOpponent);
    }

    private BooleanExpression collaborating(QChatRoom room) {
        var proposal = new QCampaignProposal("activeProposal");
        var brand = new QChatRoomMember("activeBrand");
        var creator = new QChatRoomMember("activeCreator");
        return JPAExpressions.selectOne().from(proposal)
                .join(brand).on(brand.roomId.eq(room.id).and(brand.userId.eq(proposal.brand.user.id)))
                .join(creator).on(creator.roomId.eq(room.id).and(creator.userId.eq(proposal.creator.id)))
                .where(proposal.status.eq(MATCHED),
                        proposal.campaign.isNotNull(), proposal.campaign.isDeleted.isFalse(),
                        proposal.campaign.status.notIn(COMPLETED, CampaignStatus.CANCELLED),
                        proposal.brand.user.isDeleted.isFalse(), proposal.creator.isDeleted.isFalse(),
                        brand.isDeleted.isFalse(), brand.leftAt.isNull(), creator.isDeleted.isFalse(), creator.leftAt.isNull())
                .exists();
    }

    @Override
    public Set<Long> findCollaboratingRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(queryFactory.select(ROOM.id).from(ROOM)
                .where(ROOM.id.in(roomIds), collaborating(ROOM)).fetch());
    }
}
