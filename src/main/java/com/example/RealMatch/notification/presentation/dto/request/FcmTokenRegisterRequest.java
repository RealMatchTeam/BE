package com.example.RealMatch.notification.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FcmTokenRegisterRequest(

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Size(max = 500, message = "FCM 토큰은 500자 이내여야 합니다.")
        String token,

        @Size(max = 255, message = "디바이스 정보는 255자 이내여야 합니다.")
        String deviceInfo
) {
}
