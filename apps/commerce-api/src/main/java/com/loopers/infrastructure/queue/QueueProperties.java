package com.loopers.infrastructure.queue;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "queue")
public record QueueProperties(
        Scheduler scheduler,
        Token token,
        Polling polling
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

    public record Polling(
            List<Tier> tiers,
            long defaultIntervalSeconds
    ) {
        public record Tier(
                long maxPosition,
                long intervalSeconds
        ) {
        }
    }
}
