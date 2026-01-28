package com.example.RealMatch.attachment.domain.exception;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;

@Getter
public class AttachmentException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public AttachmentException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AttachmentException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AttachmentException(BaseErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
