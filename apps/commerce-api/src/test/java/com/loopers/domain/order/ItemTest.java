package com.loopers.domain.order;

import com.loopers.domain.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    void 주문항목을_생성하면_totalPrice가_productPrice와_quantity의_곱으로_계산된다() {
        // given & when
        OrderItem orderItem = OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(Money.of(50000))
                .quantity(3)
                .build();

        // then
        assertThat(orderItem.getTotalPrice()).isEqualTo(Money.of(150000));
        assertThat(orderItem.getProductName()).isEqualTo("운동화");
        assertThat(orderItem.getProductPrice()).isEqualTo(Money.of(50000));
        assertThat(orderItem.getQuantity()).isEqualTo(3);
    }
}
