package com.example.RealMatch.global.oauth.dto;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

import lombok.Getter;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long userId;
    private final String role;
    private final AuthProvider provider;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(
            Long userId,
            String role,
            AuthProvider provider,
            Map<String, Object> attributes
    ) {
        this.userId = userId;
        this.role = role;
        this.provider = provider;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // Jwt 써서 null
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
