// NaverUserInfo.java
package com.example.RealMatch.global.config.oauth;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

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
    public String getProvider() {
        return "naver";
    }
}
