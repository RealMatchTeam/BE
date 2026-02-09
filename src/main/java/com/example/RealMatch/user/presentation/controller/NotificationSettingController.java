package com.example.RealMatch.user.presentation.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.application.service.NotificationSettingService;
import com.example.RealMatch.user.presentation.dto.request.NotificationSettingUpdateRequest;
import com.example.RealMatch.user.presentation.dto.response.NotificationSettingResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/me/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    /**
     * 내 알림 설정 조회
     */
    @GetMapping
    public CustomResponse<NotificationSettingResponse> getMyNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return CustomResponse.ok(
                notificationSettingService.getMySetting(userDetails.getUserId())
        );
    }

    /**
     * 내 알림 설정 전체 수정 (설정 완료 버튼)
     */
    @PutMapping
    public CustomResponse<String> updateMyNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationSettingUpdateRequest request
    ) {
        notificationSettingService.updateSetting(
                userDetails.getUserId(),
                request
        );
        return CustomResponse.ok("알림 설정이 수정되었습니다.");
    }
}
