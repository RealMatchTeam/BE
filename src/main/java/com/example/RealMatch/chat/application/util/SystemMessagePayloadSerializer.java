package com.example.RealMatch.chat.application.util;

import com.example.RealMatch.chat.application.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;

public interface SystemMessagePayloadSerializer {
    String serialize(ChatSystemMessagePayload payload);

    ChatSystemMessagePayload deserialize(ChatSystemMessageKind kind, String rawPayload);
}
