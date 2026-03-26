package com.loopers.application.coupon;

import com.loopers.domain.order.event.OrderCancelledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderEventHandler handler;

    @Test
    void 쿠폰이_적용된_주문_취소_이벤트를_수신하면_쿠폰을_복원한다() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(10L, List.of(
                new OrderCancelledEvent.ItemStock(1L, 2)
        ));

        // when
        handler.handleOrderCancelled(event);

        // then
        verify(couponService).restoreCoupon(10L);
    }

    @Test
    void 쿠폰이_미적용된_주문_취소_이벤트를_수신하면_쿠폰_복원을_호출하지_않는다() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(null, List.of(
                new OrderCancelledEvent.ItemStock(1L, 2)
        ));

        // when
        handler.handleOrderCancelled(event);

        // then
        verify(couponService, never()).restoreCoupon(any());
    }
}
