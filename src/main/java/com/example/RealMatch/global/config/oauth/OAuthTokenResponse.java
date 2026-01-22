package com.example.RealMatch.global.config.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OAuthTokenResponse {

    private String accessToken;
    private String refreshToken;
}
