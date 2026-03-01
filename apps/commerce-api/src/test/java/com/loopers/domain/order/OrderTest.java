package com.loopers.domain.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void 주문_생성_시_기본_상태는_COMPLETED이다() {
        // given & when
        Order order = Order.builder().userId(1L).build();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void 주문을_취소하면_상태가_CANCELLED로_변경된다() {
        // given
        Order order = Order.builder().userId(1L).build();

        // when
        order.cancel();

        // then
        assertThat(order.isCancelled()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void 본인_userId와_일치하면_isOwnedBy가_true를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();

        // when & then
        assertThat(order.isOwnedBy(1L)).isTrue();
    }

    @Test
    void 다른_userId이면_isOwnedBy가_false를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();

        // when & then
        assertThat(order.isOwnedBy(999L)).isFalse();
    }

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
