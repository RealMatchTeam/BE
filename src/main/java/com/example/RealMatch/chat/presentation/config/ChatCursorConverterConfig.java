package com.example.RealMatch.chat.presentation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.conversion.RoomCursor;

/**
 * cursor 요청 파라미터를 RoomCursor/MessageCursor로 바인딩하는 설정
 */
@Configuration
public class ChatCursorConverterConfig {

    @Bean
    public Converter<String, RoomCursor> roomCursorConverter() {
        return RoomCursor::decode;
    }

    @Bean
    public Converter<String, MessageCursor> messageCursorConverter() {
        return MessageCursor::decode;
    }
}
