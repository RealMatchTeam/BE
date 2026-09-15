package com.example.RealMatch.chat.application.service.message;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.dto.response.ChatMessageResponse;
import com.example.RealMatch.chat.application.dto.websocket.ChatSendMessageCommand;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageSocketService {

    private final ChatMessageCommandService chatMessageCommandService;

    // The failed save transaction has ended before retrying, so the duplicate can be read safely.
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 1))
    @NonNull
    public ChatMessageResponse sendMessage(ChatSendMessageCommand command, Long senderId) {
        return chatMessageCommandService.saveMessage(command, senderId);
    }
}
