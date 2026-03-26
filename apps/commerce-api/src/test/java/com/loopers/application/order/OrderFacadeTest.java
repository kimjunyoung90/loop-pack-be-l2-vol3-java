package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.order.command.OrderCreateCommand;
import com.loopers.application.order.result.OrderResult.OrderItemResult;
import com.loopers.application.order.result.OrderResult;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;

import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @Mock
    private CouponService couponService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    void 유효한_상품으로_주문하면_재고_차감_이벤트를_발행하고_OrderResult를_반환한다() {
        // given
        ProductResult productResult = new ProductResult(1L, 1L, "운동화", 50000, 8, ZonedDateTime.now(), ZonedDateTime.now());

        OrderCreateCommand command = new OrderCreateCommand(1L, null, List.of(
                new OrderCreateCommand.OrderItem(1L, 2)
        ));

        given(productService.getProduct(1L)).willReturn(productResult);

        ZonedDateTime now = ZonedDateTime.now();
        OrderResult expectedResult = new OrderResult(1L, 1L, null, "COMPLETED", 100000, 0, 100000, List.of(
                new OrderItemResult(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.placeOrder(eq(1L), any(), anyList(), eq(0))).willReturn(expectedResult);

        // when
        OrderResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.totalAmount()).isEqualTo(100000);

        ArgumentCaptor<OrderPlacedEvent> captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        OrderPlacedEvent event = captor.getValue();
        assertThat(event.stockItems()).hasSize(1);
        assertThat(event.stockItems().get(0).productId()).isEqualTo(1L);
        assertThat(event.stockItems().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void 존재하지_않는_상품으로_주문하면_예외가_발생한다() {
        // given
        OrderCreateCommand command = new OrderCreateCommand(1L, null, List.of(
                new OrderCreateCommand.OrderItem(999L, 2)
        ));
        willThrow(new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."))
                .given(productService).getProduct(999L);

        // when & then
        assertThatThrownBy(() -> orderFacade.placeOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 주문을_취소하면_재고_복원_이벤트를_발행한다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderResult cancelledResult = new OrderResult(1L, 1L, null, "CANCELLED", 100000, 0, 100000, List.of(
                new OrderItemResult(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledResult);

        // when
        OrderResult result = orderFacade.cancelOrder(1L, 1L);

        // then
        assertThat(result.status()).isEqualTo("CANCELLED");

        ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        OrderCancelledEvent event = captor.getValue();
        assertThat(event.stockItems()).hasSize(1);
        assertThat(event.stockItems().get(0).productId()).isEqualTo(1L);
        assertThat(event.stockItems().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void 본인_주문이_아니면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 취소할 수 있습니다."))
                .given(orderService).cancelOrder(999L, 1L);

        // when & then
        assertThatThrownBy(() -> orderFacade.cancelOrder(999L, 1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 이미_취소된_주문이면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 주문입니다."))
                .given(orderService).cancelOrder(1L, 1L);

        // when & then
        assertThatThrownBy(() -> orderFacade.cancelOrder(1L, 1L))
                .isInstanceOf(CoreException.class);
    }

    // --- 쿠폰 적용 주문 생성 테스트 ---

    @Test
    void 쿠폰을_적용하여_주문하면_할인이_반영된_OrderResult를_반환한다() {
        // given
        ProductResult productResult = new ProductResult(1L, 1L, "운동화", 50000, 8, ZonedDateTime.now(), ZonedDateTime.now());

        OrderCreateCommand command = new OrderCreateCommand(1L, 10L, List.of(
                new OrderCreateCommand.OrderItem(1L, 2)
        ));

        given(productService.getProduct(1L)).willReturn(productResult);
        given(couponService.useCoupon(10L, 1L, 100000)).willReturn(5000);

        ZonedDateTime now = ZonedDateTime.now();
        OrderResult expectedResult = new OrderResult(1L, 1L, 10L, "COMPLETED", 100000, 5000, 95000, List.of(
                new OrderItemResult(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.placeOrder(eq(1L), eq(10L), anyList(), eq(5000))).willReturn(expectedResult);

        // when
        OrderResult result = orderFacade.placeOrder(command);

        // then
        assertThat(result.discountAmount()).isEqualTo(5000);
        assertThat(result.finalAmount()).isEqualTo(95000);
        verify(couponService).useCoupon(10L, 1L, 100000);
    }

    @Test
    void 쿠폰_검증에_실패하면_주문이_생성되지_않는다() {
        // given
        ProductResult productResult = new ProductResult(1L, 1L, "운동화", 50000, 8, ZonedDateTime.now(), ZonedDateTime.now());

        OrderCreateCommand command = new OrderCreateCommand(1L, 10L, List.of(
                new OrderCreateCommand.OrderItem(1L, 2)
        ));

        given(productService.getProduct(1L)).willReturn(productResult);
        willThrow(new CoreException(ErrorType.FORBIDDEN, "본인 소유의 쿠폰만 사용할 수 있습니다."))
                .given(couponService).useCoupon(10L, 1L, 100000);

        // when & then
        assertThatThrownBy(() -> orderFacade.placeOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.FORBIDDEN));

        verify(orderService, never()).placeOrder(any(), any(), anyList(), anyInt());
    }

    // --- 쿠폰 적용 주문 취소 테스트 ---

    @Test
    void 쿠폰이_적용된_주문을_취소하면_쿠폰이_복원된다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderResult cancelledResult = new OrderResult(1L, 1L, 10L, "CANCELLED", 100000, 5000, 95000, List.of(
                new OrderItemResult(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledResult);

        // when
        OrderResult result = orderFacade.cancelOrder(1L, 1L);

        // then
        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(eventPublisher).publishEvent(any(OrderCancelledEvent.class));
        verify(couponService).restoreCoupon(10L);
    }

    @Test
    void 쿠폰_미적용_주문을_취소하면_쿠폰_복원이_호출되지_않는다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderResult cancelledResult = new OrderResult(1L, 1L, null, "CANCELLED", 100000, 0, 100000, List.of(
                new OrderItemResult(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledResult);

        // when
        orderFacade.cancelOrder(1L, 1L);

        // then
        verify(couponService, never()).restoreCoupon(any());
    }
}
