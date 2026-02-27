package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

@Tag(name = "Order V1 API", description = "주문 관련 API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(
        summary = "주문 생성",
        description = "주문을 생성합니다."
    )
    ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(OrderV1Dto.CreateOrderRequest request);

    @Operation(
        summary = "주문 취소",
        description = "주문을 취소합니다. 본인의 주문만 취소할 수 있으며, 취소 시 재고가 복원됩니다."
    )
    ApiResponse<OrderV1Dto.CancelOrderResponse> cancelOrder(AuthUser authUser, Long orderId);

    @Operation(
        summary = "주문 상세 조회",
        description = "주문 상세 정보를 조회합니다. 본인의 주문만 조회할 수 있습니다."
    )
    ApiResponse<OrderV1Dto.GetOrderResponse> getOrder(AuthUser authUser, Long orderId);

    @Operation(
        summary = "주문 목록 조회",
        description = "본인의 주문 목록을 기간별로 조회합니다."
    )
    ApiResponse<Page<OrderV1Dto.GetOrderResponse>> getOrders(
            AuthUser authUser, LocalDate startDate, LocalDate endDate, int page, int size);
}
