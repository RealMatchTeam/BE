package com.example.RealMatch.chat.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.chat.presentation.conversion.MessageCursor;

public record ChatMessageListResponse(
        List<ChatMessageResponse> messages,
        MessageCursor nextCursor,
        boolean hasNext
) {
}
