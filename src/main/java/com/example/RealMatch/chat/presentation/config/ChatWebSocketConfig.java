package com.example.RealMatch.chat.presentation.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origin}")
    private String allowedOrigin;
    private final ObjectProvider<HandshakeHandler> handshakeHandlerProvider;

    public ChatWebSocketConfig(ObjectProvider<HandshakeHandler> handshakeHandlerProvider) {
        this.handshakeHandlerProvider = handshakeHandlerProvider;
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        var registration = registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(allowedOrigin, "http://localhost:8080");
        HandshakeHandler handshakeHandler = handshakeHandlerProvider.getIfAvailable();
        if (handshakeHandler != null) {
            registration.setHandshakeHandler(handshakeHandler);
        }
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
