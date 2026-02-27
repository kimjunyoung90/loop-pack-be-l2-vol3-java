package com.loopers.application.order;

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
    public OrderInfo createOrder(Long userId, List<OrderItemCommand> items) {
        Order order = Order.builder()
                .userId(userId)
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

        return OrderInfo.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<OrderInfo> getOrders(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, startDate, endDate, pageable)
                .map(OrderInfo::from);
    }
}
