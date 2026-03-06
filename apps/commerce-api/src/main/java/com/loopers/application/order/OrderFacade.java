package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public OrderInfo createOrder(CreateOrderCommand command) {
        // 1. 주문 상품의 재고를 차감한다.
        List<OrderItemCommand> orderItemCommands = new ArrayList<>();
        for (CreateOrderCommand.CreateOrderItemCommand item : command.orderItems()) {
            ProductInfo product = productService.deductStock(item.productId(), item.quantity());
            orderItemCommands.add(new OrderItemCommand(
                    product.id(), product.name(), product.price(), item.quantity()));
        }

        // 2. 쿠폰을 적용한다. (쿠폰이 있는 경우)
        int discountAmount = 0;
        if (command.userCouponId() != null) {
            int totalAmount = OrderItemCommand.calculateTotalAmount(orderItemCommands);
            discountAmount = couponService.useCoupon(command.userCouponId(), command.userId(), totalAmount);
        }

        // 3. 주문을 생성한다.
        return orderService.createOrder(command.userId(), command.userCouponId(), orderItemCommands, discountAmount);
    }

    @Transactional
    public OrderInfo cancelOrder(Long userId, Long orderId) {
        // 1. 주문을 취소한다.
        OrderInfo orderInfo = orderService.cancelOrder(userId, orderId);

        // 2. 재고를 복원한다.
        for (OrderInfo.OrderItemInfo item : orderInfo.orderItems()) {
            productService.restoreStock(item.productId(), item.quantity());
        }

        // 3. 쿠폰을 복원한다. (쿠폰이 있는 경우)
        if (orderInfo.userCouponId() != null) {
            couponService.restoreCoupon(orderInfo.userCouponId());
        }

        return orderInfo;
    }
}
