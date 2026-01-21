package com.example.RealMatch.chat.application.service.message;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.presentation.dto.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

public interface ChatMessageCommandService {
    @NonNull
    ChatMessageResponse saveMessage(ChatSendMessageCommand command, Long senderId);

    @NonNull
    ChatMessageResponse saveSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    );
}
