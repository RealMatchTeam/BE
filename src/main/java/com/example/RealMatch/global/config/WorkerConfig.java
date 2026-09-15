package com.example.RealMatch.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class WorkerConfig {
    @Bean
    public ThreadPoolTaskScheduler attachmentScheduler() {
        return scheduler("attachment-");
    }
    @Bean
    public ThreadPoolTaskScheduler notificationPublisherScheduler() {
        return scheduler("outbox-");
    }
    @Bean
    public ThreadPoolTaskScheduler notificationRecoveryExecutor() {
        return scheduler("recovery-");
    }
    @Bean
    public ThreadPoolTaskScheduler socketScheduler() {
        return scheduler("socket-");
    }

    private ThreadPoolTaskScheduler scheduler(String prefix) {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix(prefix);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }
}
