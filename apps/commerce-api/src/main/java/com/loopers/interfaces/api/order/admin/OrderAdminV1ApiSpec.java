package com.loopers.interfaces.api.order.admin;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order Admin V1 API", description = "주문 관련 관리자 API 입니다.")
public interface OrderAdminV1ApiSpec {

    @Operation(
        summary = "주문 상세 조회",
        description = "주문 상세 정보를 조회합니다."
    )
    ApiResponse<OrderAdminV1Dto.GetOrderResponse> getOrder(Long orderId);
}
