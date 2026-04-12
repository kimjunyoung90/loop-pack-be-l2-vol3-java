package com.loopers.interfaces.api.queue.response;

import com.loopers.application.queue.result.QueueTokenResult;

public record QueueEnterResponse(
        String status,
        long position,
        long estimatedWaitSeconds,
        long pollingIntervalSeconds
) {
    public static QueueEnterResponse from(QueueTokenResult result) {
        return new QueueEnterResponse(
                result.status(),
                result.position(),
                result.estimatedWaitSeconds(),
                result.pollingIntervalSeconds()
        );
    }
}
