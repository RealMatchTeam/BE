package com.example.RealMatch.chat.application.util;

import org.springframework.stereotype.Component;

import com.example.RealMatch.chat.domain.enums.ChatMessageType;

@Component
public class MessagePreviewGeneratorImpl implements MessagePreviewGenerator {

    private static final int MAX_PREVIEW_LENGTH = 255;
    private static final int TRUNCATE_LENGTH = 252;

    @Override
    public String generate(ChatMessageType messageType, String content) {
        if (messageType == ChatMessageType.TEXT) {
            if (content == null || content.isBlank()) {
                return "";
            }
            // 줄바꿈 및 연속 공백을 단일 공백으로 변환
            String preview = content.replaceAll("\\s+", " ").trim();
            if (preview.length() > MAX_PREVIEW_LENGTH) {
                preview = preview.substring(0, TRUNCATE_LENGTH) + "...";
            }
            return preview;
        } else if (messageType == ChatMessageType.IMAGE) {
            return "사진을 보냈습니다";
        } else if (messageType == ChatMessageType.FILE) {
            return "파일을 보냈습니다";
        } else if (messageType == ChatMessageType.SYSTEM) {
            return "시스템 메시지";
        }
        return "";
    }
}
