package com.example.RealMatch.global.presentation.advice;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(GeneralErrorCode.UNAUTHORIZED.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        CustomResponse<?> body =
                CustomResponse.onFailure(
                        GeneralErrorCode.UNAUTHORIZED,
                        null
                );

        response.getWriter()
                .write(objectMapper.writeValueAsString(body));
    }
}
