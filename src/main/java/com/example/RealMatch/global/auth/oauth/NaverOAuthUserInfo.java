package com.example.RealMatch.global.auth.oauth;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaverOAuthUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public NaverOAuthUserInfo(final Map<String, Object> attributes) {
        this.attributes = attributes;
        log.debug("=== Naver OAuth Attributes ===");
        log.debug("Full attributes: {}", attributes);
    }

    @Override
    public String getProviderId() {
        final Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        if (response == null) {
            log.warn("Naver response is null");
            return null;
        }

        final String id = (String) response.get("id");
        log.debug("Naver ProviderId: {}", id);
        return id;
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        final Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        if (response == null) {
            log.warn("Naver response is null");
            return "no-email@naver.com";
        }

        final String email = (String) response.get("email");
        log.debug("Naver Email: {}", email);
        return email != null ? email : "no-email@naver.com";
    }

    @Override
    public String getName() {
        final Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        if (response == null) {
            log.warn("Naver response is null");
            return "네이버사용자";
        }

        final String name = (String) response.get("name");
        log.debug("Naver Name: {}", name);
        return name != null ? name : "네이버사용자";
    }
}
