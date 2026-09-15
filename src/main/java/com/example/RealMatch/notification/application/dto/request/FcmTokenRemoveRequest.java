package com.example.RealMatch.notification.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRemoveRequest(

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        String token
) {
}
