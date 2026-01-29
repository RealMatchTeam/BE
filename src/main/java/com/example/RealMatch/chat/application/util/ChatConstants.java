package com.example.RealMatch.chat.application.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatConstants {

    public static final int MAX_MESSAGE_PREVIEW_LENGTH = 255;
    public static final int TRUNCATE_LENGTH = 252;
    public static final String UNKNOWN_OPPONENT_NAME = "알 수 없음";
    public static final int SYSTEM_MESSAGE_SCHEMA_VERSION = 1;
}
