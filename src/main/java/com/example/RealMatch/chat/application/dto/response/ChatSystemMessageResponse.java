package com.example.RealMatch.chat.application.dto.response;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

public record ChatSystemMessageResponse(
        int schemaVersion,
        ChatSystemMessageKind kind,
        ChatSystemMessagePayload payload
) {
}
