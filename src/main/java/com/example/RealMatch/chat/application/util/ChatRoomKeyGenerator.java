package com.example.RealMatch.chat.application.util;

import com.example.RealMatch.chat.presentation.code.ChatErrorCode;
import com.example.RealMatch.global.exception.CustomException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomKeyGenerator {

    public static String createDirectRoomKey(Long userAId, Long userBId) {
        if (userAId == null || userBId == null) {
            throw new CustomException(ChatErrorCode.INVALID_ROOM_REQUEST);
        }
        long smallerId = Math.min(userAId, userBId);
        long largerId = Math.max(userAId, userBId);
        return String.format("direct:%d:%d", smallerId, largerId);
    }
}
