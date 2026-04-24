package com.loopers.application.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.coupon.CouponService;
import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.order.command.OrderItemCommand;
import com.loopers.application.order.result.OrderResult;
import com.loopers.application.outbox.OrderOutboxEventService;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private static final String TOPIC_ORDER = "order-events";
    private static final String EVENT_ORDER_PLACED = "ORDER_PLACED";

    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final OrderOutboxEventService orderOutboxEventService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
	
	@Transactional
    public OrderResult placeOrder(OrderCreateCommand command) {
        // 1. 주문 상품 정보를 조회한다.
        List<OrderItemCommand> orderItemCommands = command.orderItems().stream()
                .map(item -> {
                    ProductResult product = productService.getProduct(item.productId());
                    return new OrderItemCommand(
                            product.id(), product.name(), product.price(), item.quantity());
                })
                .toList();

        // 2. 재고 차감 이벤트 아이템을 생성한다.
        List<OrderPlacedEvent.ItemStock> stockItems = command.orderItems().stream()
                .map(item -> new OrderPlacedEvent.ItemStock(item.productId(), item.quantity()))
                .toList();

        // 2. 쿠폰 할인 금액을 계산한다. (쿠폰이 있는 경우)
        Money totalAmount = OrderItemCommand.calculateTotalAmount(orderItemCommands);
        Money discountAmount = Money.ZERO;
        if (command.userCouponId() != null) {
            discountAmount = couponService.calculateDiscount(command.userCouponId(), command.userId(), totalAmount);
        }

        // 3. 주문을 생성한다.
        OrderResult orderResult = orderService.placeOrder(command.userId(), command.idempotencyKey(), command.userCouponId(), orderItemCommands, discountAmount);

        // 4. 주문 생성 이벤트를 발행한다. (재고 차감, 쿠폰 사용)
        eventPublisher.publishEvent(new OrderPlacedEvent(stockItems, command.userCouponId(), command.userId(), totalAmount));

        // 5. 집계용 Outbox 메시지 저장 (상품별)
        stockItems.forEach(stockItem -> orderOutboxEventService.saveAndPublish(
                TOPIC_ORDER,
                String.valueOf(stockItem.productId()),
                writeJson(Map.of(
                        "eventType", EVENT_ORDER_PLACED,
                        "productId", stockItem.productId(),
                        "quantity", stockItem.quantity()
                ))
        ));

        return orderResult;
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @Transactional
    public OrderResult cancelOrder(Long userId, Long orderId) {
        // 1. 주문을 취소한다.
        OrderResult orderResult = orderService.cancelOrder(userId, orderId);

        // 2. 주문 취소 이벤트를 발행한다. (재고 복원, 쿠폰 복원)
        List<OrderCancelledEvent.ItemStock> stockItems = orderResult.orderItems().stream()
                .map(item -> new OrderCancelledEvent.ItemStock(item.productId(), item.quantity()))
                .toList();
        eventPublisher.publishEvent(new OrderCancelledEvent(orderResult.userCouponId(), stockItems));

        return orderResult;
    }
}
