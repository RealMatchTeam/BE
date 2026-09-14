package com.example.RealMatch.notification.application.exception;

public class PermanentSendFailureException extends RuntimeException {

    public PermanentSendFailureException(String message) {
        super(message);
    }

    public PermanentSendFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
