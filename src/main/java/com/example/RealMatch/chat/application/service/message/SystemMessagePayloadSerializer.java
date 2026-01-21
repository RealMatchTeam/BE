package com.example.RealMatch.chat.application.service.message;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;

public interface SystemMessagePayloadSerializer {
    String serialize(ChatSystemMessagePayload payload);

    ChatSystemMessagePayload deserialize(ChatSystemMessageKind kind, String rawPayload);
}
