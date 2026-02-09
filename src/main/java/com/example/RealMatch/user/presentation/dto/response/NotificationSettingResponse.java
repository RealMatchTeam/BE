package com.example.RealMatch.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingResponse {
    private boolean marketingConsent;
    private boolean appPushEnabled;
    private boolean emailEnabled;

}

