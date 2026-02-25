package com.loopers.domain.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void 주문에_주문항목을_추가하면_orderItems에_추가되고_totalPrice가_누적된다() {
        // given
        Order order = Order.builder().userId(1L).build();
        OrderItem orderItem = OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build();

        // when
        order.addOrderItem(orderItem);

        // then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getTotalPrice()).isEqualTo(100000);
        assertThat(orderItem.getOrder()).isEqualTo(order);
    }

    @Test
    void 여러_주문항목을_추가하면_totalPrice가_모든_항목의_합계가_된다() {
        // given
        Order order = Order.builder().userId(1L).build();
        OrderItem item1 = OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build();
        OrderItem item2 = OrderItem.builder()
                .productId(2L)
                .productName("슬리퍼")
                .productPrice(30000)
                .quantity(1)
                .build();

        // when
        order.addOrderItem(item1);
        order.addOrderItem(item2);

        // then
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getTotalPrice()).isEqualTo(130000);
    }
}
