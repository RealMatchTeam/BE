package com.example.RealMatch.user.presentation.swagger;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.user.presentation.dto.request.NotificationUpdateRequestDto;
import com.example.RealMatch.user.presentation.dto.response.NotificationSettingResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Notification", description = "알림 설정 API")
public interface NotificationSwagger {

    @Operation(summary = "내 알림 설정 조회", description = "로그인한 유저의 알림 설정 리스트를 조회합니다.")
    CustomResponse<List<NotificationSettingResponseDto>> getNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(summary = "알림 설정 변경", description = "특정 알림 타입 및 채널의 수신 여부를 변경합니다.")
    CustomResponse<Void> updateNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "알림 설정 변경 요청 DTO", required = true)
            @Valid @RequestBody NotificationUpdateRequestDto request
    );
}
