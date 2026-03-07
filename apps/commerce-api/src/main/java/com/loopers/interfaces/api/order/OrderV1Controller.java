package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderService;
import com.loopers.application.order.command.CreateOrderCommand;
import com.loopers.application.order.command.CreateOrderItemCommand;
import com.loopers.application.order.result.OrderResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.order.request.CreateOrderRequest;
import com.loopers.interfaces.api.order.response.CancelOrderResponse;
import com.loopers.interfaces.api.order.response.CreateOrderResponse;
import com.loopers.interfaces.api.order.response.GetOrderResponse;
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
    public ApiResponse<CreateOrderResponse> createOrder(
            @LoginUser AuthUser authUser,
            @Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                authUser.id(),
                request.userCouponId(),
                request.orderItems().stream()
                        .map(item -> new CreateOrderItemCommand(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList()
        );

        OrderResult orderResult = orderFacade.createOrder(command);
        return ApiResponse.success(CreateOrderResponse.from(orderResult));
    }

    @Override
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<CancelOrderResponse> cancelOrder(
            @LoginUser AuthUser authUser,
            @PathVariable Long orderId) {
        OrderResult orderResult = orderFacade.cancelOrder(authUser.id(), orderId);
        return ApiResponse.success(CancelOrderResponse.from(orderResult));
    }

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<GetOrderResponse> getOrder(
            @LoginUser AuthUser authUser,
            @PathVariable Long orderId) {
        OrderResult orderResult = orderService.getOrder(authUser.id(), orderId);
        return ApiResponse.success(GetOrderResponse.from(orderResult));
    }

    @Override
    @GetMapping
    public ApiResponse<Page<GetOrderResponse>> getOrders(
            @LoginUser AuthUser authUser,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GetOrderResponse> orders = orderService.getOrders(
                        authUser.id(), startDate, endDate, PageRequest.of(page, size))
                .map(GetOrderResponse::from);
        return ApiResponse.success(orders);
    }
}
