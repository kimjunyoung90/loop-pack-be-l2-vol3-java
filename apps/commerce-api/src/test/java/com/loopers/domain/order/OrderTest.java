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
        order.cancel(1L);

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
    void 주문에_주문항목을_추가하면_orderItems에_추가되고_totalAmount가_누적된다() {
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
        assertThat(order.getTotalAmount()).isEqualTo(100000);
    }

    @Test
    void 여러_주문항목을_추가하면_totalAmount가_모든_항목의_합계가_된다() {
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
        assertThat(order.getTotalAmount()).isEqualTo(130000);
    }

    @Test
    void 할인을_적용하면_할인_금액과_최종_결제_금액이_계산된다() {
        // given
        Order order = Order.builder().userId(1L).build();
        order.addOrderItem(OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build());

        // when
        order.applyDiscount(10000);

        // then
        assertThat(order.getTotalAmount()).isEqualTo(100000);
        assertThat(order.getDiscountAmount()).isEqualTo(10000);
        assertThat(order.getFinalAmount()).isEqualTo(90000);
    }

    @Test
    void 할인_금액이_상품_총액보다_크면_최종_결제_금액은_0이다() {
        // given
        int productPrice = 5000;
        int discountAmount = 10000;
        Order order = Order.builder().userId(1L).build();
        order.addOrderItem(OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(productPrice)
                .quantity(1)
                .build());

        // when
        order.applyDiscount(discountAmount);

        // then
        assertThat(discountAmount).isGreaterThan(order.getTotalAmount());
        assertThat(order.getFinalAmount()).isZero();
    }

    @Test
    void 쿠폰이_적용된_주문은_쿠폰_적용_여부가_true이다() {
        // given
        Order order = Order.builder().userId(1L).userCouponId(1L).build();

        // when & then
        assertThat(order.hasCoupon()).isTrue();
    }

    @Test
    void 쿠폰_미적용_주문은_쿠폰_적용_여부가_false이다() {
        // given
        Order order = Order.builder().userId(1L).build();

        // when & then
        assertThat(order.hasCoupon()).isFalse();
    }
}
