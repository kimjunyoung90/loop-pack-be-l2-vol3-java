package com.loopers.interfaces.api.order;

import com.loopers.application.order.CreateOrderCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;
    private final OrderService orderService;

    @Override
    @PostMapping
    public ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(
            @LoginUser AuthUser authUser,
            @Valid @RequestBody OrderV1Dto.CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                authUser.id(),
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

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<OrderV1Dto.GetOrderResponse> getOrder(
            @LoginUser AuthUser authUser,
            @PathVariable Long orderId) {
        OrderInfo orderInfo = orderService.getOrder(authUser.id(), orderId);
        return ApiResponse.success(OrderV1Dto.GetOrderResponse.from(orderInfo));
    }

    @Override
    @GetMapping
    public ApiResponse<Page<OrderV1Dto.GetOrderResponse>> getOrders(
            @LoginUser AuthUser authUser,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderV1Dto.GetOrderResponse> orders = orderService.getOrders(
                        authUser.id(), startDate, endDate, PageRequest.of(page, size))
                .map(OrderV1Dto.GetOrderResponse::from);
        return ApiResponse.success(orders);
    }
}
