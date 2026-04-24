package com.loopers.application.order;

import com.loopers.application.order.command.OrderItemCommand;
import com.loopers.application.order.result.OrderResult;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResult placeOrder(Long userId, String idempotencyKey, Long userCouponId, List<OrderItemCommand> items, Money discountAmount) {
		if (orderRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
			throw new CoreException(ErrorType.BAD_REQUEST, "이미 처리된 주문입니다.");
		}

        Order order = Order.builder()
                .userId(userId)
                .idempotencyKey(idempotencyKey)
                .userCouponId(userCouponId)
                .build();

        for (OrderItemCommand item : items) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(item.productId())
                    .productName(item.productName())
                    .productPrice(item.productPrice())
                    .quantity(item.quantity())
                    .build();
            order.addOrderItem(orderItem);
        }

        order.applyDiscount(discountAmount);

        return OrderResult.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResult getOrder(Long orderId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
        return OrderResult.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResult getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
        if (!order.isOwnedBy(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 조회할 수 있습니다.");
        }
        return OrderResult.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResult> getOrders(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, startDate, endDate, pageable)
                .map(OrderResult::from);
    }

    @Transactional
    public OrderResult completeOrder(Long orderId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
        order.complete();
        return OrderResult.from(order);
    }

    @Transactional
    public OrderResult cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));

        order.cancel(userId);

        return OrderResult.from(order);
    }
}
