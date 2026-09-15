package com.example.RealMatch.chat;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import com.example.RealMatch.chat.presentation.websocket.config.ChatWebSocketJwtInterceptor;
import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

class ChatSocketSecurityTest {
    private final JwtProvider jwt = new JwtProvider(Base64.getEncoder().encodeToString(new byte[32]), 60000, 120000, "");
    private final UserRepository users = mock(UserRepository.class);
    private final ChatWebSocketJwtInterceptor interceptor = new ChatWebSocketJwtInterceptor(jwt, users);

    @Test
    void rejectsRefreshTokensAndInactiveUsersEvenWithInheritedPrincipal() {
        var attributes = new HashMap<String, Object>();
        var refresh = frame(StompCommand.CONNECT, attributes);
        refresh.setNativeHeader("Authorization", "Bearer " + jwt.createRefreshToken(1L, "p", "CREATOR", "a@example.com"));
        refresh.setUser(() -> "already-authenticated");
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(MessageBuilder.createMessage(new byte[0], refresh.getMessageHeaders()), null));
        var access = frame(StompCommand.CONNECT, attributes);
        access.setNativeHeader("Authorization", "Bearer " + jwt.createAccessToken(1L, "p", "CREATOR", "a@example.com"));
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(MessageBuilder.createMessage(new byte[0], access.getMessageHeaders()), null));
    }

    @Test
    void establishedSessionStopsReceivingAndSendingAfterExpiry() throws Exception {
        var attributes = new HashMap<String, Object>();
        var session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("socket-1");
        when(session.getAttributes()).thenReturn(attributes);
        interceptor.decorate(mock(WebSocketHandler.class)).afterConnectionEstablished(session);
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().build()));
        var connect = frame(StompCommand.CONNECT, attributes);
        connect.setNativeHeader("Authorization", "Bearer " + jwt.createAccessToken(1L, "p", "CREATOR", "a@example.com"));
        interceptor.preSend(MessageBuilder.createMessage(new byte[0], connect.getMessageHeaders()), null);
        attributes.put("expiresAt", System.currentTimeMillis() - 1);
        var send = frame(StompCommand.SEND, attributes);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(MessageBuilder.createMessage(new byte[0], send.getMessageHeaders()), null));
        var outbound = frame(StompCommand.MESSAGE, attributes);
        assertNull(interceptor.outbound().preSend(MessageBuilder.createMessage(new byte[0], outbound.getMessageHeaders()), null));
        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test
    void sendingIsRateLimitedAndWithdrawalRevokesExistingCredentials() {
        var attributes = new HashMap<String, Object>();
        attributes.put("expiresAt", System.currentTimeMillis() + 60000);
        attributes.put("userId", 1L);
        attributes.put("rateSecond", System.currentTimeMillis() / 1000);
        attributes.put("rateCount", 20);
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().build()));
        var send = frame(StompCommand.SEND, attributes);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(MessageBuilder.createMessage(new byte[0], send.getMessageHeaders()), null));
        when(users.findById(1L)).thenReturn(Optional.empty());
        attributes.put("rateCount", 0);
        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(MessageBuilder.createMessage(new byte[0], send.getMessageHeaders()), null));
    }

    private StompHeaderAccessor frame(StompCommand command, Map<String, Object> attributes) {
        var frame = StompHeaderAccessor.create(command);
        frame.setSessionId("socket-1");
        frame.setSessionAttributes(attributes);
        frame.setLeaveMutable(true);
        return frame;
    }
}
