package com.example.RealMatch.chat.presentation.resolver;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

@Component
public class ChatUserIdResolver {

    public Long resolve(Principal principal) {
        if (principal == null) {
            throw new CustomException(GeneralErrorCode.UNAUTHORIZED);
        }
        Long userIdFromPrincipal = extractUserId(principal);
        if (userIdFromPrincipal != null) {
            return userIdFromPrincipal;
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            throw new CustomException(GeneralErrorCode.UNAUTHORIZED);
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
