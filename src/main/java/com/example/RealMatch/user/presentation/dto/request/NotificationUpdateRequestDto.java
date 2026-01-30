package com.example.RealMatch.user.presentation.dto.request;

import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.entity.enums.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "알림 설정 변경 요청 DTO")
public class NotificationUpdateRequestDto {

    @Schema(description = "알림 타입 (예: MARKETING, ACTIVITY 등)", example = "MARKETING")
    @NotNull
    private NotificationType type;

    @Schema(description = "알림 채널 (예: EMAIL, PUSH, SMS)", example = "EMAIL")
    @NotNull
    private NotificationChannel channel;

    @Schema(description = "알림 수신 여부", example = "true")
    private boolean isEnabled;
}
