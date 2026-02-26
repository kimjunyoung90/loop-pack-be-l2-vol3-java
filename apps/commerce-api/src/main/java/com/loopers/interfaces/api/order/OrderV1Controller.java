package com.loopers.interfaces.api.order;

import com.loopers.application.order.CreateOrderCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @Override
    @PostMapping
    public ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(
            @Valid @RequestBody OrderV1Dto.CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.userId(),
                request.orderItems().stream()
                        .map(item -> new CreateOrderCommand.CreateOrderItemCommand(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList()
        );

        OrderInfo orderInfo = orderFacade.createOrder(command);
        return ApiResponse.success(OrderV1Dto.CreateOrderResponse.from(orderInfo));
    }

    @Override
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderV1Dto.CancelOrderResponse> cancelOrder(
            @LoginUser AuthUser authUser,
            @PathVariable Long orderId) {
        OrderInfo orderInfo = orderFacade.cancelOrder(authUser.id(), orderId);
        return ApiResponse.success(OrderV1Dto.CancelOrderResponse.from(orderInfo));
    }
}
