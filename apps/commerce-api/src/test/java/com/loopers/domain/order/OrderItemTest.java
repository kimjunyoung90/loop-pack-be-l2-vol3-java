package com.loopers.domain.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    @Test
    void 주문항목을_생성하면_totalPrice가_productPrice와_quantity의_곱으로_계산된다() {
        // given & when
        OrderItem orderItem = OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(3)
                .build();

        // then
        assertThat(orderItem.getTotalPrice()).isEqualTo(150000);
        assertThat(orderItem.getProductName()).isEqualTo("운동화");
        assertThat(orderItem.getProductPrice()).isEqualTo(50000);
        assertThat(orderItem.getQuantity()).isEqualTo(3);
    }
}
