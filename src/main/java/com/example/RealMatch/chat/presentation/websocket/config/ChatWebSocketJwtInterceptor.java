package com.example.RealMatch.chat.presentation.websocket.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.example.RealMatch.user.application.service.UserWithdrawService.UserWithdrawn;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatWebSocketJwtInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;
    private final UserRepository users;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (message == null) {
            return null;
        }
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        var command = accessor.getCommand();
        if (command == StompCommand.DISCONNECT || command == null) {
            return message;
        }
        var attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            throw new MessageDeliveryException("Session required");
        }
        if (command == StompCommand.CONNECT) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null) {
                header = accessor.getFirstNativeHeader("authorization");
            }
            if (header == null || !header.startsWith("Bearer ")) {
                throw new MessageDeliveryException("JWT required");
            }
            String token = header.substring(7);
            var claims = jwtProvider.getClaims(token);
            if (!"access".equals(claims.get("type", String.class))) {
                throw new MessageDeliveryException("Invalid access token");
            }
            Long id = Long.valueOf(claims.getSubject());
            if (users.findById(id).isEmpty()) {
                throw new MessageDeliveryException("Inactive user");
            }
            // ponytail: at most 2,000 local sessions; add a per-user index if CONNECT contention grows.
            synchronized (sessions) {
                if (sessions.values().stream().filter(s -> id.equals(s.getAttributes().get("userId"))).count() >= 5) {
                    throw new MessageDeliveryException("Session limit exceeded");
                }
                attributes.put("userId", id);
                attributes.put("expiresAt", claims.getExpiration().getTime());
            }
            var details = new CustomUserDetails(id, claims.get("providerId", String.class), claims.get("role", String.class), claims.get("email", String.class));
            accessor.setUser(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        } else if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            requireActive(attributes);
            synchronized (attributes) {
                long second = System.currentTimeMillis() / 1000;
                if (!Long.valueOf(second).equals(attributes.get("rateSecond"))) {
                    attributes.put("rateSecond", second);
                    attributes.put("rateCount", 0);
                }
                int count = (int) attributes.get("rateCount") + 1;
                attributes.put("rateCount", count);
                if (count > 20) {
                    throw new MessageDeliveryException("Message rate limit exceeded");
                }
                if (command == StompCommand.SUBSCRIBE) {
                    var subscriptions = (Set<String>) attributes.computeIfAbsent("subscriptions", key -> new HashSet<String>());
                    if (accessor.getSubscriptionId() == null || subscriptions.size() >= 100) {
                        throw new MessageDeliveryException("Subscription limit exceeded");
                    }
                    subscriptions.add(accessor.getSubscriptionId());
                }
            }
        } else if (command == StompCommand.UNSUBSCRIBE) {
            synchronized (attributes) {
                var subscriptions = (Set<?>) attributes.get("subscriptions");
                if (subscriptions != null) {
                    subscriptions.remove(accessor.getSubscriptionId());
                }
            }
        }
        return message;
    }

    public ChannelInterceptor outbound() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var accessor = StompHeaderAccessor.wrap(message);
                if (accessor.getCommand() == StompCommand.MESSAGE) {
                    var session = sessions.get(accessor.getSessionId());
                    if (session == null) {
                        return null;
                    }
                    try {
                        requireActive(session.getAttributes());
                    } catch (RuntimeException ex) {
                        close(session);
                        return null;
                    }
                }
                return message;
            }
        };
    }

    private void requireActive(Map<String, Object> attributes) {
        var expiresAt = (Long) attributes.get("expiresAt");
        var id = (Long) attributes.get("userId");
        if (expiresAt == null || expiresAt <= System.currentTimeMillis() || id == null || users.findById(id).isEmpty()) {
            throw new MessageDeliveryException("Expired or inactive session");
        }
    }

    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                synchronized (sessions) {
                    if (sessions.size() >= 2000) {
                        session.close(CloseStatus.SERVICE_OVERLOAD);
                        return;
                    }
                    session.getAttributes().put("expiresAt", System.currentTimeMillis() + 60_000);
                    sessions.put(session.getId(), session);
                }
                try {
                    super.afterConnectionEstablished(session);
                } catch (Exception ex) {
                    sessions.remove(session.getId());
                    throw ex;
                }
            }
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                sessions.remove(session.getId());
                super.afterConnectionClosed(session, status);
            }
        };
    }

    @Scheduled(fixedDelay = 30_000, scheduler = "socketScheduler")
    public void expireSessions() {
        sessions.values().stream().filter(s -> (Long) s.getAttributes().getOrDefault("expiresAt", 0L) <= System.currentTimeMillis()).forEach(this::close);
    }

    @TransactionalEventListener
    public void withdrawn(UserWithdrawn event) {
        sessions.values().stream().filter(s -> event.userId().equals(s.getAttributes().get("userId"))).forEach(this::close);
    }

    private void close(WebSocketSession session) {
        try {
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException ignored) {
            /* already disconnected */
        } finally {
            sessions.remove(session.getId());
        }
    }
}
