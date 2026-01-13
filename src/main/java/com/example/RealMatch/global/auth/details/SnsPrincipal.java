package com.example.RealMatch.global.auth.details;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.RealMatch.user.domain.entity.User;

public record SnsPrincipal(User user) implements OAuth2User, UserDetails {

    public static SnsPrincipal fromEntity(User user) {
        return new SnsPrincipal(user);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getName(),
                "provider", user.getProvider(),
                "providerId", user.getProviderId()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // OAuth는 Role 없음
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getPassword() {
        return ""; // OAuth는 패스워드 사용 X
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
