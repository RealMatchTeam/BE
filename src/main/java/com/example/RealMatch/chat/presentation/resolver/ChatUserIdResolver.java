package com.example.RealMatch.chat.presentation.resolver;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;

@Component
public class ChatUserIdResolver {

    public Long resolve(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }
        Long userIdFromPrincipal = extractUserId(principal);
        if (userIdFromPrincipal != null) {
            return userIdFromPrincipal;
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Principal name must be a numeric user id.");
        }
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object authPrincipal = authentication.getPrincipal();
            if (authPrincipal instanceof CustomUserDetails userDetails) {
                return userDetails.getUserId();
            }
        }
        return null;
    }
}
