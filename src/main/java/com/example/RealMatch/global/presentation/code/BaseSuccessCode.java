package com.example.RealMatch.global.presentation.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();
}
