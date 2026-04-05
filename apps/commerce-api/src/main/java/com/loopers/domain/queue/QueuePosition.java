package com.loopers.domain.queue;

public record QueuePosition(
        QueueStatus status,
        long position,
        long estimatedWaitSeconds,
        long pollingIntervalSeconds
) {
}
