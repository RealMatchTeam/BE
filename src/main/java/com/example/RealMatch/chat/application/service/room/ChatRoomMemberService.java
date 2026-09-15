package com.example.RealMatch.chat.application.service.room;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.application.util.ChatRoomMemberValidator;
import com.example.RealMatch.chat.code.ChatErrorCode;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository users;

    @NonNull
    public ChatRoomMember getActiveMemberOrThrow(@NonNull Long roomId, @NonNull Long userId) {
        if (users.findById(userId).isEmpty()) {
            throw new CustomException(ChatErrorCode.NOT_ROOM_MEMBER);
        }
        ChatRoomMember member = chatRoomMemberRepository
                .findMemberByRoomIdAndUserIdWithRoomCheck(roomId, userId)
                .orElseThrow(() -> new CustomException(ChatErrorCode.NOT_ROOM_MEMBER));

        ChatRoomMemberValidator.validateActiveMember(member);
        return member;
    }
}
