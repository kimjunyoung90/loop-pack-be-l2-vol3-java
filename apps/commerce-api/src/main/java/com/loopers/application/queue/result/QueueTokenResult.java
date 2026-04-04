package com.loopers.application.queue.result;

public record QueueTokenResult(
        long position,
        long estimatedWaitSeconds
) {
}
