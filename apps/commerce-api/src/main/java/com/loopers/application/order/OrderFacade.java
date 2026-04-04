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
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {

	private static final String TOPIC_ORDER_PLACED = "order-metrics.PLACED";
	private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;
	private final KafkaTemplate kafkaTemplate;
	
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
        int totalAmount = OrderItemCommand.calculateTotalAmount(orderItemCommands);
        int discountAmount = 0;
        if (command.userCouponId() != null) {
            discountAmount = couponService.calculateDiscount(command.userCouponId(), command.userId(), totalAmount);
        }

        // 3. 주문을 생성한다.
        OrderResult orderResult = orderService.placeOrder(command.userId(), command.userCouponId(), orderItemCommands, discountAmount);

        // 4. 주문 생성 이벤트를 발행한다. (재고 차감, 쿠폰 사용)
        eventPublisher.publishEvent(new OrderPlacedEvent(stockItems, command.userCouponId(), command.userId(), totalAmount));
		stockItems.forEach(stockItem -> {
			kafkaTemplate.send(TOPIC_ORDER_PLACED, String.valueOf(stockItem.productId()), "{\"productId\": " + stockItem.productId() + ",\"quantity\":"+ stockItem.quantity() + "}");
		});

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
