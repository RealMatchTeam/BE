package com.example.RealMatch.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomRepository;
import com.example.RealMatch.chat.application.service.room.ChatRoomCommandService;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.UserRepository;

class ChatRoomAuthorizationTest {

    private final UserRepository users = mock(UserRepository.class);
    private final ChatRoomRepository rooms = mock(ChatRoomRepository.class);
    private final ChatRoomMemberRepository members = mock(ChatRoomMemberRepository.class);
    private final ChatRoomCommandService service = new ChatRoomCommandService(
            rooms, members, mock(ChatMessageEventPublisher.class), mock(AfterCommitExecutor.class),
            users);

    @ParameterizedTest
    @CsvSource({"CREATOR,BRAND", "GUEST,CREATOR", "BRAND,ADMIN", "WITHDRAWN,CREATOR", "BRAND,BRAND"})
    void rejectsInvalidRolesForBothEntryPoints(Role brandRole, Role creatorRole) {
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().role(brandRole).build()));
        when(users.findById(2L)).thenReturn(Optional.of(User.builder().role(creatorRole).build()));

        assertEquals(ChatErrorCode.INVALID_ROOM_REQUEST,
                assertThrows(CustomException.class, () -> service.createOrGetRoomAsMember(1L, 1L, 2L)).getCode());
        assertThrows(CustomException.class, () -> service.createOrGetRoomSystem(1L, 2L));
        verifyNoInteractions(rooms, members);
    }

    @Test
    void rejectsUnknownAndDeletedParticipants() {
        assertThrows(CustomException.class, () -> service.createOrGetRoomSystem(1L, 2L));
        User deleted = User.builder().role(Role.BRAND).build();
        deleted.withdraw(1L);
        when(users.findById(1L)).thenReturn(Optional.of(deleted));
        assertThrows(CustomException.class, () -> service.createOrGetRoomSystem(1L, 2L));
        verifyNoInteractions(rooms, members);
    }

    @Test
    void rejectsNonParticipantAndMissingPrincipal() {
        assertEquals(ChatErrorCode.NOT_ROOM_MEMBER,
                assertThrows(CustomException.class, () -> service.createOrGetRoomAsMember(3L, 1L, 2L)).getCode());
        assertEquals(ChatErrorCode.NOT_ROOM_MEMBER,
                assertThrows(CustomException.class, () -> service.createOrGetRoomAsMember(null, 1L, 2L)).getCode());
        verifyNoInteractions(users, rooms, members);
    }

    @Test
    void rejectsMissingOrIdenticalParticipantIds() {
        assertThrows(CustomException.class, () -> service.createOrGetRoomSystem(null, 2L));
        assertThrows(CustomException.class, () -> service.createOrGetRoomSystem(1L, 1L));
        assertThrows(CustomException.class, () -> service.createOrGetRoomAsMember(1L, 1L, null));
        verifyNoInteractions(users, rooms, members);
    }

    @Test
    void validParticipantsCanReuseRoomFromBothEntryPoints() {
        validUsers();
        ChatRoom room = ChatRoom.createDirectRoom("direct:1:2");
        ReflectionTestUtils.setField(room, "id", 10L);
        when(rooms.findByRoomKey("direct:1:2")).thenReturn(Optional.of(room));

        assertEquals(10L, service.createOrGetRoomAsMember(2L, 1L, 2L).roomId());
        assertEquals(10L, service.createOrGetRoomSystem(1L, 2L).roomId());
        verifyNoInteractions(members);
    }

    @Test
    void validParticipantsCanCreateRoom() {
        validUsers();
        when(rooms.saveAndFlush(any(ChatRoom.class))).thenAnswer(call -> {
            ChatRoom room = call.getArgument(0);
            ReflectionTestUtils.setField(room, "id", 10L);
            return room;
        });

        assertEquals(10L, service.createOrGetRoomAsMember(1L, 1L, 2L).roomId());
        verify(members).findByRoomIdAndUserId(10L, 1L);
        verify(members).findByRoomIdAndUserId(10L, 2L);
    }

    private void validUsers() {
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(User.builder().role(Role.BRAND).build()));
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().role(Role.BRAND).build()));
        when(users.findById(2L)).thenReturn(Optional.of(User.builder().role(Role.CREATOR).build()));
    }
}
