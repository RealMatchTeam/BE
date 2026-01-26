package com.example.RealMatch.chat.application.service.message;

import org.springframework.stereotype.Service;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.fixture.ChatFixtureFactory;

@Service
public class ChatMessageQueryServiceImpl implements ChatMessageQueryService {

    @Override
    public ChatMessageListResponse getMessages(
            Long userId,
            Long roomId,
            MessageCursor messageCursor,
            int size
    ) {
        return ChatFixtureFactory.sampleMessageListResponse(roomId);
    }
}
