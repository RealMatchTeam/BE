package com.example.RealMatch.chat.application.service.socket;

import org.springframework.lang.NonNull;

import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;
import com.example.RealMatch.global.presentation.code.BaseErrorCode;

public interface ChatSocketService {
    @NonNull
    ChatMessageResponse createMessageEvent(ChatSendMessageCommand command, Long senderId);

    ChatSendMessageAck createAck(ChatSendMessageCommand command, Long messageId);

    ChatSendMessageAck createFailedAck(ChatSendMessageCommand command, BaseErrorCode errorCode);

    ChatMessageCreatedEvent createSystemMessageEvent(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    );
}
