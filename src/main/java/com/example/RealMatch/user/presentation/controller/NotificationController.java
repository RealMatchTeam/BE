package com.example.RealMatch.user.presentation.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.application.service.NotificationService;
import com.example.RealMatch.user.presentation.dto.request.NotificationUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.response.NotificationSettingResponseDto;
import com.example.RealMatch.user.presentation.swagger.NotificationSwagger;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
public class NotificationController implements NotificationSwagger {

    private final NotificationService notificationService;

    @GetMapping
    public CustomResponse<List<NotificationSettingResponseDto>> getNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(notificationService.getNotificationSettings(userDetails.getUserId()));
    }

    @PostMapping
    public CustomResponse<Void> updateNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NotificationUpdateRequestDto request
    ) {
        notificationService.updateNotificationSetting(userDetails.getUserId(), request);
        return CustomResponse.ok(null);
    }
}
