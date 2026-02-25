package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 주문을_생성하면_저장된_주문의_OrderInfo를_반환한다() {
        // given
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(1L, "운동화", 50000, 2)
        );
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderInfo result = orderService.createOrder(1L, items);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(100000);
        assertThat(result.orderItems()).hasSize(1);
        assertThat(result.orderItems().getFirst().productName()).isEqualTo("운동화");
    }

    @Test
    void 여러_상품을_주문하면_모든_주문항목이_포함된_OrderInfo를_반환한다() {
        // given
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(1L, "운동화", 50000, 2),
                new OrderItemCommand(2L, "슬리퍼", 30000, 1)
        );
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderInfo result = orderService.createOrder(1L, items);

        // then
        assertThat(result.totalPrice()).isEqualTo(130000);
        assertThat(result.orderItems()).hasSize(2);
        assertThat(result.orderItems().get(0).productName()).isEqualTo("운동화");
        assertThat(result.orderItems().get(1).productName()).isEqualTo("슬리퍼");
    }
}
