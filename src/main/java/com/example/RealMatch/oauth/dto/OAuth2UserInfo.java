package com.example.RealMatch.oauth.dto;

import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
    AuthProvider getProvider();
}
