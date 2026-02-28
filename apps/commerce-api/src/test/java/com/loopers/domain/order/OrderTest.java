package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void 주문_생성_시_기본_상태는_COMPLETED이다() {
        // given & when
        Order order = Order.builder().userId(1L).build();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void 본인_주문을_취소하면_상태가_CANCELLED로_변경된다() {
        // given
        Order order = Order.builder().userId(1L).build();

        // when
        order.cancel(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void 본인이_아닌_사용자가_취소하면_예외가_발생한다() {
        // given
        Order order = Order.builder().userId(1L).build();

        // when & then
        assertThatThrownBy(() -> order.cancel(999L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 이미_취소된_주문을_취소하면_예외가_발생한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        order.cancel(1L);

        // when & then
        assertThatThrownBy(() -> order.cancel(1L))
                .isInstanceOf(CoreException.class);
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
