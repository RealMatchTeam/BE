package com.example.RealMatch.chat.application.util;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomMemberValidator {

    public static void validateActiveMember(@NonNull ChatRoomMember member) {
        if (member.getLeftAt() != null) {
            throw new ChatException(ChatErrorCode.USER_LEFT_ROOM);
        }
    }
}
