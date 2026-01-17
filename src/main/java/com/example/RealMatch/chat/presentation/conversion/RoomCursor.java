package com.example.RealMatch.chat.presentation.conversion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record RoomCursor(LocalDateTime lastMessageAt, Long roomId) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static RoomCursor of(LocalDateTime lastMessageAt, Long roomId) {
        return new RoomCursor(lastMessageAt, roomId);
    }

    @JsonCreator
    public static RoomCursor decode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.split("\\|", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Room Cursor 형식이 올바르지 않습니다.");
        }
        LocalDateTime lastMessageAt;
        try {
            lastMessageAt = LocalDateTime.parse(parts[0], FORMATTER);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Room Cursor lastMessageAt 형식이 올바르지 않습니다.", ex);
        }
        Long roomId;
        try {
            roomId = Long.valueOf(parts[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Room Cursor roomId 형식이 올바르지 않습니다.", ex);
        }
        return new RoomCursor(lastMessageAt, roomId);
    }

    @JsonValue
    public String encode() {
        return FORMATTER.format(lastMessageAt) + "|" + roomId;
    }
}
