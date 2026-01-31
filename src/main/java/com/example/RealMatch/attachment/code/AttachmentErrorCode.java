package com.example.RealMatch.attachment.code;

import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttachmentErrorCode implements BaseErrorCode {
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "ATTACHMENT404_1",
            "첨부 파일을 찾을 수 없습니다."),
    ATTACHMENT_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN,
            "ATTACHMENT403_1",
            "첨부 파일 소유권이 일치하지 않습니다."),
    ATTACHMENT_NOT_READY(HttpStatus.CONFLICT,
            "ATTACHMENT409_1",
            "첨부 파일이 아직 사용 가능한 상태가 아닙니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST,
            "ATTACHMENT400_1",
            "유효하지 않은 파일입니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST,
            "ATTACHMENT400_2",
            "유효하지 않은 파일명입니다."),
    INVALID_FILE_SIZE(HttpStatus.BAD_REQUEST,
            "ATTACHMENT400_3",
            "유효하지 않은 파일 크기입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST,
            "ATTACHMENT400_4",
            "파일 크기가 제한을 초과했습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST,
            "ATTACHMENT400_5",
            "지원하지 않는 이미지 형식입니다."),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "ATTACHMENT500_1",
            "파일 업로드에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "ATTACHMENT500_2",
            "파일 삭제에 실패했습니다."),
    S3_ACCESS_DENIED(HttpStatus.FORBIDDEN,
            "ATTACHMENT403_2",
            "파일 스토리지 접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
