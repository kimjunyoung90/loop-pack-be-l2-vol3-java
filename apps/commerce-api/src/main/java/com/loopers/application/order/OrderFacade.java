package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.order.Order;
import com.loopers.domain.product.Product;
import com.loopers.domain.order.OrderItem;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
        Order order = orderService.findOrder(orderId);

        if (!order.isOwnedBy(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
        }

        if (order.isCancelled()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 주문입니다.");
        }

        order.cancel();

        for (OrderItem item : order.getOrderItems()) {
            Product product = productService.findProduct(item.getProductId());
            product.restoreStock(item.getQuantity());
        }

        if (order.hasCoupon()) {
            UserCoupon userCoupon = couponService.findUserCoupon(order.getUserCouponId());
            userCoupon.restore();
        }

        return OrderInfo.from(order);
    }
}
