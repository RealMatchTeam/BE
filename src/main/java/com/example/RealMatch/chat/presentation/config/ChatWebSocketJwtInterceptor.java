package com.example.RealMatch.chat.presentation.config;

import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.config.jwt.JwtProvider;

@Component
public class ChatWebSocketJwtInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public ChatWebSocketJwtInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Message<?> preSend(@Nullable Message<?> message, @Nullable MessageChannel channel) {
        if (message == null) {
            return null;
        }
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }
        Principal existingUser = accessor.getUser();
        if (existingUser != null) {
            return message;
        }
        String authHeader = resolveAuthorization(accessor);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return message;
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!jwtProvider.validateToken(token)) {
            return message;
        }
        Long userId = jwtProvider.getUserId(token);
        String providerId = jwtProvider.getProviderId(token);
        String role = jwtProvider.getRole(token);
        CustomUserDetails userDetails = new CustomUserDetails(userId, providerId, role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        accessor.setUser(authentication);
        return message;
    }

    @SuppressWarnings("null")
    private @Nullable String resolveAuthorization(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            return authHeader;
        }
        return accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION.toLowerCase());
    }
}
