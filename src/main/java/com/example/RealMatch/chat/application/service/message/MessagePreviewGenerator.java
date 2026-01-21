package com.example.RealMatch.chat.application.service.message;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;

public interface MessagePreviewGenerator {
    String generate(ChatMessageType messageType, String content);
}
