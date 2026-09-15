package com.example.RealMatch.chat.presentation.websocket.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origin}")
    private String allowedOrigin;
    @Autowired
    @Lazy
    private TaskScheduler messageBrokerTaskScheduler;

    private final ObjectProvider<HandshakeHandler> handshakeHandlerProvider;
    private final ObjectProvider<ChatWebSocketJwtInterceptor> jwtInterceptorProvider;
    private final ObjectProvider<ChatWebSocketAuthorizationInterceptor> authorizationInterceptorProvider;

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        var registration = registry.addEndpoint("/api/v1/ws/chat")
                .setAllowedOrigins(allowedOrigin.split(","));

        // HandshakeHandler는 withSockJS() 전에 설정해야 함
        HandshakeHandler handshakeHandler = handshakeHandlerProvider.getIfAvailable();
        if (handshakeHandler != null) {
            registration.setHandshakeHandler(handshakeHandler);
        }

        registration.withSockJS(); // SockJS 활성화
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue").setTaskScheduler(messageBrokerTaskScheduler).setHeartbeatValue(new long[] {10000, 10000});
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(4).maxPoolSize(8).queueCapacity(500);
        jwtInterceptorProvider.ifAvailable(registration::interceptors);
        authorizationInterceptorProvider.ifAvailable(registration::interceptors);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(4).maxPoolSize(8).queueCapacity(500);
        jwtInterceptorProvider.ifAvailable(jwt -> registration.interceptors(jwt.outbound()));
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024).setSendBufferSizeLimit(256 * 1024).setSendTimeLimit(10_000);
        jwtInterceptorProvider.ifAvailable(jwt -> registration.addDecoratorFactory(jwt::decorate));
    }
}
