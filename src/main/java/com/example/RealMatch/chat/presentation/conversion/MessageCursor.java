package com.example.RealMatch.chat.presentation.conversion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record MessageCursor(Long messageId) {

    public static MessageCursor of(Long messageId) {
        return new MessageCursor(messageId);
    }

    @JsonCreator
    public static MessageCursor decode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new MessageCursor(Long.valueOf(value));
    }

    @JsonValue
    public String encode() {
        return messageId.toString();
    }
}
