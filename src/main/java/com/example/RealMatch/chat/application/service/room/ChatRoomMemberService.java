package com.example.RealMatch.chat.application.service.room;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.util.ChatRoomMemberValidator;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @NonNull
    public ChatRoomMember getActiveMemberOrThrow(@NonNull Long roomId, @NonNull Long userId) {
        ChatRoomMember member = chatRoomMemberRepository
                .findMemberByRoomIdAndUserIdWithRoomCheck(roomId, userId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.NOT_ROOM_MEMBER));

        ChatRoomMemberValidator.validateActiveMember(member);
        return member;
    }
}
