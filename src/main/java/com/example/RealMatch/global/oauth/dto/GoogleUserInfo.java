package com.example.RealMatch.global.oauth.dto;

import java.util.Map;

import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        if (name != null) {
            return name;
        }
        return (String) attributes.get("given_name");
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
}
