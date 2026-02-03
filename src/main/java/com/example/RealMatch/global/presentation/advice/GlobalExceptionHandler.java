package com.example.RealMatch.global.presentation.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.BaseErrorCode;
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
                .body(CustomResponse.onFailure(GeneralErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponse<?>> handleConstraintViolation(ConstraintViolationException e) {

        log.warn("[ConstraintViolationException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_PAGE.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INVALID_PAGE, e.getMessage()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomResponse<?>> handleHandlerMethodValidation(HandlerMethodValidationException e) {

        log.warn("[HandlerMethodValidationException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_PAGE.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INVALID_PAGE, e.getMessage()));
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
                .body(CustomResponse.onFailure(GeneralErrorCode.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomResponse<?>> handleIllegalState(IllegalStateException e) {
        log.error("[IllegalStateException] {}", e.getMessage(), e);

        // 보안 문제가 남아 있습니다. 시스템 연동 이후 반환 값을 null로 변환해주세요
        return ResponseEntity
                .status(GeneralErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<?>> handleUnexpectedException(Exception e) {

        log.error("[UnexpectedException]", e);

        // 보안 문제가 남아 있습니다. 시스템 연동 이후 반환 값을 null로 변환해주세요
        return ResponseEntity
                .status(GeneralErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(CustomResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomResponse<?>> handleCustomException(CustomException e) {
        log.warn("[CustomException] code={}, message={}", e.getCode().getCode(), e.getMessage());
        BaseErrorCode errorCode = e.getCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CustomResponse.onFailure(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomResponse<?>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return ResponseEntity
                .badRequest()
                .body(CustomResponse.onFailure(
                        GeneralErrorCode.INVALID_DATA,
                        "날짜 형식은 yyyy-MM-dd 형식이어야 합니다."
                ));
    }
}
