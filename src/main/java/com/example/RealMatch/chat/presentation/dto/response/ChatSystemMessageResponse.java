package com.example.RealMatch.chat.presentation.dto.response;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

public record ChatSystemMessageResponse(
        int schemaVersion,
        ChatSystemMessageKind kind,
        ChatSystemMessagePayload payload
) {
}
