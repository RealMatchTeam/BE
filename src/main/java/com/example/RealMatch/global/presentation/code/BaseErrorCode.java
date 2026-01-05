package com.example.RealMatch.global.presentation.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
