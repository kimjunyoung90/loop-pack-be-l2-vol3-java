package com.loopers.application.order;

import com.loopers.application.order.result.OrderResult;
import com.loopers.application.order.result.OrderResult.OrderItemResult;
import com.loopers.domain.common.Money;
import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentRejectedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentEventHandler handler;

    @Test
    void 결제_승인_이벤트를_수신하면_주문을_완료한다() {
        // given
        PaymentApprovedEvent event = new PaymentApprovedEvent(1L);

        ZonedDateTime now = ZonedDateTime.now();
        OrderResult completedResult = new OrderResult(1L, 1L, null, "COMPLETED", Money.of(100000), Money.of(0), Money.of(100000), List.of(
                new OrderItemResult(1L, 1L, "운동화", Money.of(50000), 2, Money.of(100000), now, now)
        ), now, now);
        given(orderService.completeOrder(1L)).willReturn(completedResult);

        // when
        handler.handlePaymentApproved(event);

        // then
        verify(orderService).completeOrder(1L);
    }

    @Test
    void 결제_거절_이벤트를_수신하면_주문을_취소하고_취소_이벤트를_발행한다() {
        // given
        PaymentRejectedEvent event = new PaymentRejectedEvent(1L, 1L);

        ZonedDateTime now = ZonedDateTime.now();
        OrderResult cancelledResult = new OrderResult(1L, 1L, 10L, "CANCELLED", Money.of(100000), Money.of(5000), Money.of(95000), List.of(
                new OrderItemResult(1L, 1L, "운동화", Money.of(50000), 2, Money.of(100000), now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledResult);

        // when
        handler.handlePaymentRejected(event);

        // then
        verify(orderService).cancelOrder(1L, 1L);

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        OrderCancelledEvent cancelledEvent = captor.getValue();
        assertThat(cancelledEvent.userCouponId()).isEqualTo(10L);
        assertThat(cancelledEvent.stockItems()).hasSize(1);
        assertThat(cancelledEvent.stockItems().get(0).productId()).isEqualTo(1L);
        assertThat(cancelledEvent.stockItems().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void 쿠폰_미적용_주문의_결제_거절_시_이벤트에_쿠폰ID가_null이다() {
        // given
        PaymentRejectedEvent event = new PaymentRejectedEvent(2L, 1L);

        ZonedDateTime now = ZonedDateTime.now();
        OrderResult cancelledResult = new OrderResult(2L, 1L, null, "CANCELLED", Money.of(50000), Money.of(0), Money.of(50000), List.of(
                new OrderItemResult(2L, 3L, "티셔츠", Money.of(25000), 2, Money.of(50000), now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 2L)).willReturn(cancelledResult);

        // when
        handler.handlePaymentRejected(event);

        // then
        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userCouponId()).isNull();
    }
}
