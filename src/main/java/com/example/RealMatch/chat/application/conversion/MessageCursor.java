package com.example.RealMatch.chat.application.conversion;

import com.example.RealMatch.global.common.QueryLimits;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record MessageCursor(Long messageId) {
    public MessageCursor {
        if (messageId == null || messageId <= 0) {
            throw new IllegalArgumentException("Positive messageId required");
        }
    }

    public static MessageCursor of(Long messageId) {
        return new MessageCursor(messageId);
    }

    @JsonCreator
    public static MessageCursor decode(String value) {
        QueryLimits.text(value, 100);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new MessageCursor(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Message Cursor messageId 형식이 올바르지 않습니다.", ex);
        }
    }

    @JsonValue
    public String encode() {
        return messageId.toString();
    }
}
