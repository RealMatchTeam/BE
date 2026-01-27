package com.example.RealMatch.oauth.dto;

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
