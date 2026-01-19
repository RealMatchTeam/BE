package com.example.RealMatch.global.config.oauth;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    String getProvider();
}
