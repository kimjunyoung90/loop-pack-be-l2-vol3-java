package com.loopers.interfaces.api.queue;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.queue.response.QueueEnterResponse;
import com.loopers.interfaces.api.queue.response.QueueStatusResponse;
import com.loopers.support.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Queue V1 API", description = "대기열 관련 API 입니다.")
public interface QueueV1ApiSpec {

    @Operation(
        summary = "대기열 진입",
        description = "주문 대기열에 진입합니다. 이미 대기 중인 경우 기존 토큰과 순번을 반환합니다."
    )
    ApiResponse<QueueEnterResponse> enterQueue(AuthUser authUser);

    @Operation(
        summary = "대기열 순번 조회",
        description = "대기열에서의 현재 순번을 조회합니다."
    )
    ApiResponse<QueueStatusResponse> getQueuePosition(AuthUser authUser);
}
