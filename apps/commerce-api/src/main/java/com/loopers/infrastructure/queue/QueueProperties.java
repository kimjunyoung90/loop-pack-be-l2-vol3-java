package com.loopers.infrastructure.queue;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue")
public record QueueProperties(
        Scheduler scheduler,
        Token token
) {
    public record Scheduler(
            long intervalMs,
            int batchSize
    ) {
    }

    public record Token(
            long ttlMinutes
    ) {
    }
}
