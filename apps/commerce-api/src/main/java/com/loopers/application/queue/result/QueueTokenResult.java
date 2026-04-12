package com.loopers.application.queue.result;

public record QueueTokenResult(
        String status,
        long position,
        long estimatedWaitSeconds,
        long pollingIntervalSeconds
) {
    public static QueueTokenResult bypassed() {
        return new QueueTokenResult("BYPASSED", 0, 0, 0);
    }
}
