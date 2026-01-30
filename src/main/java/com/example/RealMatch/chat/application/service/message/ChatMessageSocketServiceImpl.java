package com.example.RealMatch.chat.application.service.message;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.chat.application.event.ChatMessageEventPublisher;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.domain.enums.ChatSystemMessageKind;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatSystemMessagePayload;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageSocketServiceImpl implements ChatMessageSocketService {

    private final ChatMessageCommandService chatMessageCommandService;
    private final AfterCommitExecutor afterCommitExecutor;
    private final ChatMessageEventPublisher eventPublisher;

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse sendMessage(ChatSendMessageCommand command, Long senderId) {
        ChatMessageResponse response = chatMessageCommandService.saveMessage(command, senderId);
        publishMessageEvents(command.roomId(), response);
        return response;
    }

    @Override
    @Transactional
    @NonNull
    public ChatMessageResponse sendSystemMessage(
            Long roomId,
            ChatSystemMessageKind kind,
            ChatSystemMessagePayload payload
    ) {
        ChatMessageResponse response = chatMessageCommandService.saveSystemMessage(roomId, kind, payload);
        publishMessageEvents(roomId, response);
        return response;
    }

    private void publishMessageEvents(Long roomId, ChatMessageResponse response) {
        afterCommitExecutor.execute(() -> {
            eventPublisher.publishMessageCreated(roomId, response);
            eventPublisher.publishRoomListUpdated(roomId);
        });
    }
}
