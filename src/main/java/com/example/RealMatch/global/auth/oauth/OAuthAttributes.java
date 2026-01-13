package com.example.RealMatch.global.auth.oauth;

import java.util.Map;

public class OAuthAttributes {

    public static OAuthUserInfo of(final String provider, final Map<String, Object> attributes) {
        return switch (provider) {
            case "kakao" -> new KakaoOAuthUserInfo(attributes);
            case "naver" -> new NaverOAuthUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
