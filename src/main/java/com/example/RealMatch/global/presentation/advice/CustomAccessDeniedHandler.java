package com.example.RealMatch.global.presentation.advice;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(GeneralErrorCode.FORBIDDEN.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        CustomResponse<?> body =
                CustomResponse.onFailure(
                        GeneralErrorCode.FORBIDDEN,
                        null
                );

        response.getWriter()
                .write(objectMapper.writeValueAsString(body));
    }
}
