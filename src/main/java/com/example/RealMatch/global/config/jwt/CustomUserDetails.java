package com.example.RealMatch.global.config.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;        // DB PK
    private final String providerId;  // 소셜 고유 ID
    private final String role;        // USER / ADMIN

    public CustomUserDetails(Long userId, String providerId, String role) {
        this.userId = userId;
        this.providerId = providerId;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role);
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return providerId; }  // 소셜 UUID 기준
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
