package com.loopers.interfaces.api.queue.response;

import com.loopers.application.queue.result.QueueTokenResult;

public record QueueEnterResponse(
        long position,
        long estimatedWaitSeconds
) {
    public static QueueEnterResponse from(QueueTokenResult result) {
        return new QueueEnterResponse(
                result.position(),
                result.estimatedWaitSeconds()
        );
    }
}
