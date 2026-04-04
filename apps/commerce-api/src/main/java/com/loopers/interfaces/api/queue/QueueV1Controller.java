package com.loopers.interfaces.api.queue;

import com.loopers.application.queue.QueueService;
import com.loopers.application.queue.result.QueuePositionResult;
import com.loopers.application.queue.result.QueueTokenResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.queue.response.QueueEnterResponse;
import com.loopers.interfaces.api.queue.response.QueueStatusResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/queue")
public class QueueV1Controller implements QueueV1ApiSpec {

    private final QueueService queueService;

    @Override
    @PostMapping("/enter")
    public ApiResponse<QueueEnterResponse> enterQueue(@LoginUser AuthUser authUser) {
        QueueTokenResult result = queueService.enterQueue(authUser.id());
        return ApiResponse.success(QueueEnterResponse.from(result));
    }

    @Override
    @GetMapping("/position")
    public ApiResponse<QueueStatusResponse> getQueuePosition(@LoginUser AuthUser authUser) {
        QueuePositionResult result = queueService.getQueuePosition(authUser.id());
        return ApiResponse.success(QueueStatusResponse.from(result));
    }
}
