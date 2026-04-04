package com.loopers.interfaces.api.queue.response;

import com.loopers.application.queue.result.QueuePositionResult;

public record QueueStatusResponse(
        String status,
        String token,
        long position,
        long estimatedWaitSeconds
) {
    public static QueueStatusResponse from(QueuePositionResult result) {
        return new QueueStatusResponse(
                result.status(),
                result.token(),
                result.position(),
                result.estimatedWaitSeconds()
        );
    }
}
