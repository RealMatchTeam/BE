package com.example.RealMatch.oauth.dto;

import java.util.Map;

import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return response != null ? (String) response.get("id") : null;
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return response != null ? (String) response.get("email") : null;
    }

    @Override
    public String getName() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return response != null ? (String) response.get("name") : null;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.NAVER;
    }
}
