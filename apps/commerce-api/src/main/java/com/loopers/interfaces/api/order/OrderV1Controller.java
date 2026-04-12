package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderService;
import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.order.result.OrderResult;
import com.loopers.application.queue.QueueService;
import com.loopers.infrastructure.kafka.OrderRequestProducer;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.order.request.OrderCreateRequest;
import com.loopers.interfaces.api.order.response.OrderCancelResponse;
import com.loopers.interfaces.api.order.response.OrderCreateResponse;
import com.loopers.interfaces.api.order.response.OrderDetailResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;
    private final OrderService orderService;
    private final QueueService queueService;
    private final OrderRequestProducer orderRequestProducer;

    @Override
    @PostMapping
    public ApiResponse<OrderCreateResponse> placeOrder(
            @LoginUser AuthUser authUser,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Entry-Token", required = false) String entryToken,
            @Valid @RequestBody OrderCreateRequest request) {
        OrderCreateCommand command = new OrderCreateCommand(
                authUser.id(),
                idempotencyKey,
                request.userCouponId(),
                request.orderItems().stream()
                        .map(item -> new OrderCreateCommand.OrderItem(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList()
        );

        if (!queueService.isRedisAvailable()) {
            orderRequestProducer.send(command);
            return ApiResponse.success(OrderCreateResponse.accepted(idempotencyKey));
        }

        queueService.validateToken(entryToken, authUser.id());
        OrderResult orderResult = orderFacade.placeOrder(command);
        queueService.removeToken(authUser.id());
        return ApiResponse.success(OrderCreateResponse.from(orderResult));
    }

    @Override
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponse> cancelOrder(
            @LoginUser AuthUser authUser,
            @PathVariable Long orderId) {
        OrderResult orderResult = orderFacade.cancelOrder(authUser.id(), orderId);
        return ApiResponse.success(OrderCancelResponse.from(orderResult));
    }

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @LoginUser AuthUser authUser,
            @PathVariable Long orderId) {
        OrderResult orderResult = orderService.getOrder(authUser.id(), orderId);
        return ApiResponse.success(OrderDetailResponse.from(orderResult));
    }

    @Override
    @GetMapping
    public ApiResponse<PageResponse<OrderDetailResponse>> getOrders(
            @LoginUser AuthUser authUser,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderDetailResponse> orders = orderService.getOrders(
                        authUser.id(), startDate, endDate, pageable)
                .map(OrderDetailResponse::from);
        return ApiResponse.success(PageResponse.from(orders));
    }
}
