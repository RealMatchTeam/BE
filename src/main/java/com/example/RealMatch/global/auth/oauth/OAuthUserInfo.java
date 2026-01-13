package com.example.RealMatch.global.auth.oauth;

public interface OAuthUserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    String getProvider();
}
