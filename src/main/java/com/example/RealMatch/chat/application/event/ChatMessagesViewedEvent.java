package com.example.RealMatch.chat.application.event;

public record ChatMessagesViewedEvent(
        Long memberId,
        Long latestMessageId
) {
}
