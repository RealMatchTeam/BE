package com.example.RealMatch.chat.application.service.message;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

public interface ChatMessageSocketService {
    @NonNull
    ChatMessageResponse sendMessage(ChatSendMessageCommand command, Long senderId);
}
