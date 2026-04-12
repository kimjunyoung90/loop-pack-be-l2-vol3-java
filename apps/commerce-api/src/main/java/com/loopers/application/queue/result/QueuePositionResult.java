package com.loopers.application.queue.result;

import com.loopers.domain.queue.QueuePosition;

public record QueuePositionResult(
        String status,
        String token,
        long position,
        long estimatedWaitSeconds,
        long pollingIntervalSeconds,
        long orderableAfterSeconds
) {
    public static QueuePositionResult from(QueuePosition queuePosition, String token) {
        return new QueuePositionResult(
                queuePosition.status().name(),
                token,
                queuePosition.position(),
                queuePosition.estimatedWaitSeconds(),
                queuePosition.pollingIntervalSeconds(),
                queuePosition.orderableAfterSeconds()
        );
    }
}
