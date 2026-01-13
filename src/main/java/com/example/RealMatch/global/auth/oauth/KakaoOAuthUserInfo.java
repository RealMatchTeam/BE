package com.example.RealMatch.global.auth.oauth;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoOAuthUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        log.debug("=== Kakao OAuth Attributes ===");
        log.debug("Full attributes: {}", attributes);
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        log.debug("Kakao ProviderId: {}", id);
        return String.valueOf(id);
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount == null) {
            log.warn("kakao_account is null");
            return "no-email@kakao.com";
        }

        String email = (String) kakaoAccount.get("email");
        log.debug("Kakao Email: {}", email);

        return email != null ? email : "no-email@kakao.com";
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            if (profile != null) {
                String nickname = (String) profile.get("nickname");
                log.debug("Kakao Nickname: {}", nickname);

                if (nickname != null && !nickname.isEmpty()) {
                    return nickname;
                }
            }
        }

        log.warn("Kakao nickname not found, using default");
        return "카카오사용자";
    }
}
