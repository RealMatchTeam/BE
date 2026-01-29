package com.example.RealMatch.chat.application.util;

import java.util.List;

import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.chat.presentation.code.ChatErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomValidator {

    public static void validateDirectRoomOpponent(List<ChatRoomMember> opponentMembers, Long roomId) {
        if (opponentMembers == null || opponentMembers.isEmpty()) {
            throw new ChatException(
                    ChatErrorCode.INTERNAL_ERROR,
                    "Chat room opponent not found (data integrity issue). roomId=" + roomId
            );
        }
        if (opponentMembers.size() != 1) {
            throw new ChatException(
                    ChatErrorCode.INTERNAL_ERROR,
                    "Chat room is not 1:1. roomId=" + roomId + ", opponentCount=" + opponentMembers.size()
            );
        }
    }
}
