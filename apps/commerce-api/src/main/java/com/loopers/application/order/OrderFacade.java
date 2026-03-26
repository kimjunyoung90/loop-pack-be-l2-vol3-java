package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.order.command.OrderItemCommand;
import com.loopers.application.order.result.OrderResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResult placeOrder(OrderCreateCommand command) {
        // 1. 주문 상품 정보를 조회한다.
        List<OrderItemCommand> orderItemCommands = new ArrayList<>();
        List<OrderPlacedEvent.ItemStock> stockItems = new ArrayList<>();
        for (OrderCreateCommand.OrderItem item : command.orderItems()) {
            ProductResult product = productService.getProduct(item.productId());
            orderItemCommands.add(new OrderItemCommand(
                    product.id(), product.name(), product.price(), item.quantity()));
            stockItems.add(new OrderPlacedEvent.ItemStock(item.productId(), item.quantity()));
        }

        // 2. 쿠폰을 적용한다. (쿠폰이 있는 경우)
        int discountAmount = 0;
        if (command.userCouponId() != null) {
            int totalAmount = OrderItemCommand.calculateTotalAmount(orderItemCommands);
            discountAmount = couponService.useCoupon(command.userCouponId(), command.userId(), totalAmount);
        }

        // 3. 주문을 생성한다.
        OrderResult orderResult = orderService.placeOrder(command.userId(), command.userCouponId(), orderItemCommands, discountAmount);

        // 4. 재고 차감 이벤트를 발행한다.
        eventPublisher.publishEvent(new OrderPlacedEvent(stockItems));

        return orderResult;
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
