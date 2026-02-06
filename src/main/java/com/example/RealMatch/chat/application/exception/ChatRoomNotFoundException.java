package com.example.RealMatch.chat.application.exception;

/**
 * 채팅방을 찾을 수 없을 때 발생하는 예외.
 * 논리적 실패이므로 재시도하지 않아야 함.
 */
public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(String message) {
        super(message);
    }
}
