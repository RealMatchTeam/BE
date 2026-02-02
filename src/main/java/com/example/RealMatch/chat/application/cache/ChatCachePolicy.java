package com.example.RealMatch.chat.application.cache;

import java.time.Duration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatCachePolicy {
    public static final Duration ROOM_LIST_TTL = Duration.ofSeconds(30);
    public static final Duration ROOM_DETAIL_TTL = Duration.ofSeconds(60);
}
