package com.example.RealMatch.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.attachment.application.service.AttachmentQueryService;
import com.example.RealMatch.chat.application.dto.response.ChatProposalStatusNoticePayloadResponse;
import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.mapper.ChatMessageResponseMapper;
import com.example.RealMatch.chat.application.repository.ChatMessageRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.application.service.message.ChatMessageQueryService;
import com.example.RealMatch.chat.application.service.message.ChatMessageSocketService;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberCommandService;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.application.util.JacksonSystemMessagePayloadSerializer;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatProposalStatus;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.resolver.ChatUserIdResolver;
import com.example.RealMatch.chat.presentation.websocket.controller.ChatSocketController;
import com.example.RealMatch.global.common.QueryLimits;
import com.example.RealMatch.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ChatReadAndPayloadTest {
    @Test
    void socketReadFailureReturnsAnAcknowledgementInsteadOfDisappearing() {
        var reads = mock(ChatRoomMemberCommandService.class);
        var resolver = mock(ChatUserIdResolver.class);
        java.security.Principal principal = () -> "2";
        when(resolver.resolve(principal)).thenReturn(2L);
        doThrow(new CustomException(ChatErrorCode.INVALID_ROOM_FOR_MESSAGE)).when(reads).markRead(1L, 2L, 99L);
        var controller = new ChatSocketController(mock(ChatMessageSocketService.class), resolver, reads);
        var ack = controller.markRead(new ChatSocketController.ReadCommand(1L, 99L), principal);
        assertFalse(ack.success());
        assertEquals(ChatErrorCode.INVALID_ROOM_FOR_MESSAGE.getCode(), ack.errorCode());
    }

    @Test
    void historyQueryHasNoReadSideEffectsAndRejectsUnboundedPage() {
        var messages = mock(ChatMessageRepository.class);
        var membership = mock(ChatRoomMemberService.class);
        var service = new ChatMessageQueryService(messages, membership, mock(AttachmentQueryService.class), mock(ChatMessageResponseMapper.class));
        when(messages.findMessagesByRoomId(1L, null, 20)).thenReturn(List.of());
        service.getMessages(2L, 1L, null, 20);
        verify(messages).findMessagesByRoomId(1L, null, 20);
        verifyNoMoreInteractions(messages);
        assertThrows(CustomException.class, () -> service.getMessages(2L, 1L, null, Integer.MAX_VALUE));
        assertThrows(CustomException.class, () -> QueryLimits.page(-1, 20));
        assertThrows(CustomException.class, () -> QueryLimits.page(0, 0));
    }

    @Test
    void readCommandRejectsAnotherRoomsMessageAndPublishesOnlyAfterCommit() {
        var members = mock(ChatRoomMemberRepository.class);
        var membership = mock(ChatRoomMemberService.class);
        var messages = mock(ChatMessageRepository.class);
        var publisher = mock(ChatMessageEventPublisher.class);
        var afterCommit = mock(AfterCommitExecutor.class);
        var member = ChatRoomMember.create(1L, 2L, ChatRoomMemberRole.CREATOR);
        ReflectionTestUtils.setField(member, "id", 3L);
        when(membership.getActiveMemberOrThrow(1L, 2L)).thenReturn(member);
        var service = new ChatRoomMemberCommandService(members, membership, messages, publisher, afterCommit);
        assertThrows(CustomException.class, () -> service.markRead(1L, 2L, 99L));
        verifyNoInteractions(members, afterCommit, publisher);
        when(messages.existsByIdAndRoomId(4L, 1L)).thenReturn(true);
        when(members.updateLastReadMessageIfNewer(eq(3L), eq(4L), any())).thenReturn(1);
        service.markRead(1L, 2L, 4L);
        verify(afterCommit).execute(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void oldFlatPayloadAndVersionedPayloadRemainReadable() throws Exception {
        var mapper = new ObjectMapper().findAndRegisterModules();
        var serializer = new JacksonSystemMessagePayloadSerializer(mapper);
        String legacy = "{\"proposalId\":1,\"actorUserId\":2,\"processedAt\":\"2026-09-14T12:00:00\",\"proposalStatus\":\"MATCHED\"}";
        var payload = new ChatProposalStatusNoticePayloadResponse(1L, 2L, LocalDateTime.of(2026, 9, 14, 12, 0), ChatProposalStatus.MATCHED);
        assertEquals(payload, serializer.deserialize(ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE, legacy));
        String stored = serializer.serialize(payload);
        assertEquals(1, mapper.readTree(stored).path("schemaVersion").asInt());
        assertEquals(payload, serializer.deserialize(ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE, stored));
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE, "null"));
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE, "{\"schemaVersion\":1,\"payload\":null}"));
        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(ChatSystemMessageKind.PROPOSAL_STATUS_NOTICE, "{\"schemaVersion\":99,\"payload\":{}}"));
    }
}
