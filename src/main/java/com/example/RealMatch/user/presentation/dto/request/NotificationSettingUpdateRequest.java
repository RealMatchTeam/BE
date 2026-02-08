package com.example.RealMatch.user.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationSettingUpdateRequest {
    private boolean marketingConsent;
    private boolean appPushEnabled;
    private boolean emailEnabled;

}