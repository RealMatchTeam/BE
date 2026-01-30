package com.example.RealMatch.chat.presentation.websocket.config;

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

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatWebSocketJwtInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(@Nullable Message<?> message, @Nullable MessageChannel channel) {
        if (message == null) {
            return null;
        }
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        if (accessor.getUser() != null) {
            return message;
        }
        String authHeader = resolveAuthorization(accessor);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // TODO: 인증 시스템 완성 후 연결 거부 처리 활성화
            // 인증 헤더가 없거나 형식이 맞지 않으면 연결 거부

            return message;
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        if (!jwtProvider.validateToken(token)) {
            // TODO: 인증 시스템 완성 후 연결 거부 처리 활성화
            // JWT 검증 실패 시 연결 거부

            return message;
        }
        Long userId = jwtProvider.getUserId(token);
        String providerId = jwtProvider.getProviderId(token);
        String role = jwtProvider.getRole(token);
        String email = jwtProvider.getEmail(token);
        CustomUserDetails userDetails = new CustomUserDetails(userId, providerId, role, email);
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
