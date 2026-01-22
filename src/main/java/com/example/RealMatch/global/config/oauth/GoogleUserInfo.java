package com.example.RealMatch.global.config.oauth;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

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
    public String getProvider() {
        return "google";
    }
}
