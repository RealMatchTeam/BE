package com.example.RealMatch.chat.application.conversion;

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
