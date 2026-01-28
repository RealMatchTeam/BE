package com.example.RealMatch.chat.presentation.code;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND,
            "CHAT404_1",
            "채팅방을 찾을 수 없습니다."),
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "CHAT404_2",
            "첨부 파일을 찾을 수 없습니다."),
    NOT_ROOM_MEMBER(HttpStatus.FORBIDDEN,
            "CHAT403_2",
            "채팅방 멤버가 아닙니다."),
    USER_LEFT_ROOM(HttpStatus.FORBIDDEN,
            "CHAT403_3",
            "이미 나간 채팅방입니다."),
    ATTACHMENT_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN,
            "CHAT403_4",
            "첨부 파일 소유권이 일치하지 않습니다."),
    INVALID_ROOM_FOR_MESSAGE(HttpStatus.BAD_REQUEST,
            "CHAT400_1",
            "메시지가 속한 채팅방이 일치하지 않습니다."),
    INVALID_ROOM_REQUEST(HttpStatus.BAD_REQUEST,
            "CHAT400_2",
            "채팅방 생성 요청이 올바르지 않습니다."),
    INVALID_MESSAGE_FORMAT(HttpStatus.BAD_REQUEST,
            "CHAT400_3",
            "메시지 형식이 올바르지 않습니다."),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT,
            "CHAT409_1",
            "동일한 메시지 ID로 다른 내용의 메시지가 전송되었습니다."),
    MESSAGE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "CHAT500_1",
            "메시지 저장에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "CHAT500_2",
            "내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
