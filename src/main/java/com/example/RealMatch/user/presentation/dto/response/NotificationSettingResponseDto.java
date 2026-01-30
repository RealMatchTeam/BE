package com.example.RealMatch.user.presentation.dto.response;

import com.example.RealMatch.user.domain.entity.NotificationSetting;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.entity.enums.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "알림 설정 조회 응답 DTO")
public class NotificationSettingResponseDto {

    @Schema(description = "알림 타입")
    private NotificationType type;

    @Schema(description = "알림 채널")
    private NotificationChannel channel;

    @Schema(description = "알림 수신 여부")
    private boolean isEnabled;

    public static NotificationSettingResponseDto from(NotificationSetting setting) {
        return NotificationSettingResponseDto.builder()
                .type(setting.getType())
                .channel(setting.getChannel())
                .isEnabled(setting.isEnabled())
                .build();
    }
}
