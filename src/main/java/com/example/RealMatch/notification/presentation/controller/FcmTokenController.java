package com.example.RealMatch.notification.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.notification.application.service.FcmTokenService;
import com.example.RealMatch.notification.presentation.dto.request.FcmTokenRegisterRequest;
import com.example.RealMatch.notification.presentation.dto.request.FcmTokenRemoveRequest;
import com.example.RealMatch.notification.presentation.swagger.FcmTokenSwagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "FCM Token", description = "FCM 디바이스 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/fcm/tokens")
@RequiredArgsConstructor
public class FcmTokenController implements FcmTokenSwagger {

    private final FcmTokenService fcmTokenService;

    @Override
    @PostMapping
    public CustomResponse<Void> registerToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRegisterRequest request
    ) {
        fcmTokenService.registerToken(userDetails.getUserId(), request.token(), request.deviceInfo());
        return CustomResponse.ok(null);
    }

    @Override
    @DeleteMapping
    public CustomResponse<Void> removeToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRemoveRequest request
    ) {
        fcmTokenService.removeToken(request.token());
        return CustomResponse.ok(null);
    }
}
