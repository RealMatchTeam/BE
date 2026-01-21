package com.example.RealMatch.chat.application.service.message;

import com.example.RealMatch.chat.presentation.conversion.MessageCursor;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;

public interface ChatMessageQueryService {
    ChatMessageListResponse getMessages(
            CustomUserDetails user,
            Long roomId,
            MessageCursor messageCursor,
            int size
    );
}
