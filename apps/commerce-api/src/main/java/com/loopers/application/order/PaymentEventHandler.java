package com.loopers.application.order;

import com.loopers.application.order.result.OrderResult;
import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentEventHandler {

    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handlePaymentApproved(PaymentApprovedEvent event) {
        orderService.completeOrder(event.orderId());
    }

    @EventListener
    public void handlePaymentRejected(PaymentRejectedEvent event) {
        // 1. 주문을 취소한다.
        OrderResult orderResult = orderService.cancelOrder(event.userId(), event.orderId());

        // 2. 주문 취소 이벤트를 발행한다. (재고 복원, 쿠폰 복원)
        List<OrderCancelledEvent.ItemStock> stockItems = orderResult.orderItems().stream()
                .map(item -> new OrderCancelledEvent.ItemStock(item.productId(), item.quantity()))
                .toList();
        eventPublisher.publishEvent(new OrderCancelledEvent(orderResult.userCouponId(), stockItems));
    }
}
