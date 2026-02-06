package com.example.RealMatch.chat.application.event;

public record ChatMessagesViewedEvent(
        Long memberId,
        Long userId,
        Long latestMessageId
) {
}
