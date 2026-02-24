package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order V1 API", description = "주문 관련 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(
        summary = "주문 생성",
        description = "주문을 생성합니다."
    )
    ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(OrderV1Dto.CreateOrderRequest request);
}
