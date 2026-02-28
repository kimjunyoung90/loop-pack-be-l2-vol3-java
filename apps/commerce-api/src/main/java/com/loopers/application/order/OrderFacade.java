package com.loopers.application.order;

import com.loopers.application.product.ProductService;
import com.loopers.application.user.UserService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.product.Product;
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
    public OrderInfo cancelOrder(Long userId, Long orderId) {
        Order order = orderService.findOrder(orderId);

        order.cancel(userId);

        for (OrderItem item : order.getOrderItems()) {
            Product product = productService.findProduct(item.getProductId());
            product.restoreStock(item.getQuantity());
        }

        return OrderInfo.from(order);
    }
}
