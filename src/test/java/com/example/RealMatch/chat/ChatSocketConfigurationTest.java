package com.example.RealMatch.chat;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.example.RealMatch.chat.application.repository.ChatMessageRepository;
import com.example.RealMatch.chat.application.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.application.service.room.ChatRoomReadService;
import com.example.RealMatch.chat.application.tx.AfterCommitExecutor;
import com.example.RealMatch.chat.presentation.resolver.ChatUserIdResolver;
import com.example.RealMatch.chat.presentation.websocket.WebSocketChatMessageEventPublisher;
import com.example.RealMatch.chat.presentation.websocket.config.ChatWebSocketAuthorizationInterceptor;
import com.example.RealMatch.chat.presentation.websocket.config.ChatWebSocketConfig;
import com.example.RealMatch.global.config.WorkerConfig;
import com.example.RealMatch.user.domain.repository.UserRepository;

class ChatSocketConfigurationTest {
    @Test
    void boundedChannelsAndHeartbeatSchedulerStartTogether() {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, "cors.allowed-origin=https://example.com");
            context.register(ChatWebSocketConfig.class, WorkerConfig.class);
            context.refresh();
            org.junit.jupiter.api.Assertions.assertTrue(context.containsBean("messageBrokerTaskScheduler"));
        }
    }

    @Test
    void authorizationAndPublishingBeansDoNotFormACycle() {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context, "cors.allowed-origin=https://example.com");
            context.register(
                    MockDependencies.class,
                    ChatWebSocketConfig.class,
                    WorkerConfig.class,
                    ChatRoomMemberService.class,
                    ChatRoomReadService.class,
                    ChatWebSocketAuthorizationInterceptor.class,
                    WebSocketChatMessageEventPublisher.class
            );

            context.refresh();

            org.junit.jupiter.api.Assertions.assertNotNull(
                    context.getBean(ChatRoomReadService.class));
            org.junit.jupiter.api.Assertions.assertNotNull(
                    context.getBean(WebSocketChatMessageEventPublisher.class));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MockDependencies {

        @Bean
        ChatRoomMemberRepository chatRoomMemberRepository() {
            return mock(ChatRoomMemberRepository.class);
        }

        @Bean
        ChatMessageRepository chatMessageRepository() {
            return mock(ChatMessageRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        AfterCommitExecutor afterCommitExecutor() {
            return mock(AfterCommitExecutor.class);
        }

        @Bean
        ChatUserIdResolver chatUserIdResolver() {
            return mock(ChatUserIdResolver.class);
        }
    }
}
