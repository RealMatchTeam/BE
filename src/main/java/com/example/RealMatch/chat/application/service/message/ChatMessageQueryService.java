package com.example.RealMatch.chat.application.service.message;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;

public interface ChatMessageQueryService {
    ChatMessageListResponse getMessages(
            Long userId,
            Long roomId,
            MessageCursor messageCursor,
            int size
    );
}
