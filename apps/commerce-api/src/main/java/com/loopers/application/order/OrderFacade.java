package com.loopers.application.order;

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

    @Transactional
    public OrderInfo createOrder(CreateOrderCommand command) {
        List<OrderItemCommand> orderItemCommands = new ArrayList<>();

        for (CreateOrderCommand.CreateOrderItemCommand item : command.orderItems()) {
            ProductInfo product = productService.deductStock(item.productId(), item.quantity());

            orderItemCommands.add(new OrderItemCommand(
                    product.id(),
                    product.name(),
                    product.price(),
                    item.quantity()
            ));
        }

        return orderService.createOrder(command.userId(), orderItemCommands);
    }

    @Transactional
    public OrderInfo cancelOrder(Long userId, Long orderId) {
        OrderInfo orderInfo = orderService.cancelOrder(userId, orderId);

        for (OrderInfo.OrderItemInfo item : orderInfo.orderItems()) {
            productService.restoreStock(item.productId(), item.quantity());
        }

        return orderInfo;
    }
}
