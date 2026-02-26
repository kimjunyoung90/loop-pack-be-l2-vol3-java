package com.loopers.application.order;

import com.loopers.application.product.ProductService;
import com.loopers.application.user.UserService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
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

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    @Transactional
    public OrderInfo createOrder(CreateOrderCommand command) {
        userService.findUser(command.userId());

        List<OrderItemCommand> orderItemCommands = new ArrayList<>();

        for (CreateOrderCommand.CreateOrderItemCommand item : command.orderItems()) {
            Product product = productService.findProduct(item.productId());
            product.deductStock(item.quantity());

            orderItemCommands.add(new OrderItemCommand(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    item.quantity()
            ));
        }

        return orderService.createOrder(command.userId(), orderItemCommands);
    }

    @Transactional
    public OrderInfo cancelOrder(String loginId, String password, Long orderId) {
        User user = userService.authenticateUser(loginId, password);

        Order order = orderService.findOrder(orderId);

        if (!order.isOwnedBy(user.getId())) {
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

        return OrderInfo.from(order);
    }
}
