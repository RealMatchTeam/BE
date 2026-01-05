package com.example.RealMatch.global.presentation.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.example.RealMatch.global.presentation.ApiResponse;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {

        log.warn("[IllegalArgumentException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.BAD_REQUEST, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException e) {

        log.warn("[ConstraintViolationException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_PAGE.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.INVALID_PAGE, null));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        
        log.warn("[HandlerMethodValidationException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.INVALID_PAGE.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.INVALID_PAGE, null));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<?>> handleSecurityException(SecurityException e) {

        log.warn("[SecurityException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.UNAUTHORIZED.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.UNAUTHORIZED, null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException e) {

        log.warn("[ResourceNotFoundException] {}", e.getMessage());

        return ResponseEntity
                .status(GeneralErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.NOT_FOUND, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedException(Exception e) {

        log.error("[UnexpectedException]", e);

        return ResponseEntity
                .status(GeneralErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, null));
    }
}
