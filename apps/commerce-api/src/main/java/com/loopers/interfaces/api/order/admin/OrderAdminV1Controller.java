package com.loopers.interfaces.api.order.admin;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminOnly
@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/orders")
public class OrderAdminV1Controller implements OrderAdminV1ApiSpec {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderAdminV1Dto.GetOrderResponse> getOrder(
            @PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return ApiResponse.success(OrderAdminV1Dto.GetOrderResponse.from(orderInfo));
    }
}
