package com.smartcart.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Enables:
 *   @Async  → ActivityLogService.log() runs on a separate thread pool
 *   @Scheduled → OutboxPublisherService polls every 5 seconds
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Dedicated thread pool for async activity logging.
     * Named "activityLogExecutor" so other @Async methods use Spring's default pool.
     */
    @Bean(name = "activityLogExecutor")
    public Executor activityLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("activity-log-");
        executor.initialize();
        return executor;
    }
}