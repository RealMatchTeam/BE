package com.example.RealMatch.chat.domain.repository;

import java.util.List;

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
}
