package com.example.RealMatch.global.presentation.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.example.RealMatch.chat.domain.exception.ChatException;
import com.example.RealMatch.global.oauth.exception.AuthException;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomResponse<?>> handleIllegalArgument(IllegalArgumentException e) {

        log.warn("[IllegalArgumentException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.BAD_REQUEST.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.BAD_REQUEST, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponse<?>> handleConstraintViolation(ConstraintViolationException e) {

        log.warn("[ConstraintViolationException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_PAGE.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INVALID_PAGE, null));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomResponse<?>> handleHandlerMethodValidation(HandlerMethodValidationException e) {

        log.warn("[HandlerMethodValidationException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_PAGE.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INVALID_PAGE, null));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<CustomResponse<?>> handleSecurityException(SecurityException e) {

        log.warn("[SecurityException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.UNAUTHORIZED.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.UNAUTHORIZED, null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomResponse<?>> handleResourceNotFound(ResourceNotFoundException e) {

        log.warn("[ResourceNotFoundException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.NOT_FOUND.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.NOT_FOUND, null));
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<CustomResponse<?>> handleChatException(ChatException e) {
        log.warn("[ChatException] code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(CustomResponse.onFailure(e.getErrorCode(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomResponse<?>> handleIllegalState(IllegalStateException e) {
        log.error("[IllegalStateException] {}", e.getMessage(), e);
        return ResponseEntity
                .status(GeneralErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<?>> handleUnexpectedException(Exception e) {

        log.error("[UnexpectedException]", e);

        return ResponseEntity
                .status(GeneralErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, null));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<CustomResponse<?>> handleAuthException(AuthException e) {
        log.warn("[AuthException] code={}, message={}", e.getErrorCode().getCode(), e.getErrorCode().getMessage());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(CustomResponse.onFailure(
                        e.getErrorCode(),
                        null
                ));
    }
}
