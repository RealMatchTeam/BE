package com.example.RealMatch.chat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.example.RealMatch.chat.presentation.websocket.config.ChatWebSocketConfig;
import com.example.RealMatch.global.config.WorkerConfig;

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
}
