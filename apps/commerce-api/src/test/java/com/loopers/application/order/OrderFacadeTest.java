package com.loopers.application.order;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private OrderFacade orderFacade;

    @Test
    void 유효한_상품으로_주문하면_재고_차감_후_OrderInfo를_반환한다() {
        // given
        ProductInfo productInfo = new ProductInfo(1L, 1L, "운동화", 50000, 8, ZonedDateTime.now(), ZonedDateTime.now());

        CreateOrderCommand command = new CreateOrderCommand(1L, null, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 2)
        ));

        given(productService.deductStock(1L, 2)).willReturn(productInfo);

        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo expectedInfo = new OrderInfo(1L, 1L, null, "COMPLETED", 100000, 0, 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.createOrder(eq(1L), any(), anyList(), eq(0))).willReturn(expectedInfo);

        // when
        OrderInfo result = orderFacade.createOrder(command);

        // then
        assertThat(result.totalAmount()).isEqualTo(100000);
        verify(productService).deductStock(1L, 2);
    }

    @Test
    void 존재하지_않는_상품으로_주문하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(1L, null, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(999L, 2)
        ));
        willThrow(new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."))
                .given(productService).deductStock(999L, 2);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 재고가_부족한_상품이_포함되면_CoreException이_발생한다() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(1L, null, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 5)
        ));
        willThrow(new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다."))
                .given(productService).deductStock(1L, 5);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 주문을_취소하면_상태가_CANCELLED인_OrderInfo와_복원된_재고를_반환한다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo cancelledInfo = new OrderInfo(1L, 1L, null, "CANCELLED", 100000, 0, 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledInfo);

        // when
        OrderInfo result = orderFacade.cancelOrder(1L, 1L);

        // then
        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(productService).restoreStock(1L, 2);
    }

    @Test
    void 본인_주문이_아니면_CoreException_FORBIDDEN이_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 취소할 수 있습니다."))
                .given(orderService).cancelOrder(999L, 1L);

        // when & then
        assertThatThrownBy(() -> orderFacade.cancelOrder(999L, 1L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 이미_취소된_주문이면_CoreException_BAD_REQUEST가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 주문입니다."))
                .given(orderService).cancelOrder(1L, 1L);

        // when & then
        assertThatThrownBy(() -> orderFacade.cancelOrder(1L, 1L))
                .isInstanceOf(CoreException.class);
    }

    // --- 쿠폰 적용 주문 생성 테스트 ---

    @Test
    void 쿠폰을_적용하여_주문하면_할인이_반영된_OrderInfo를_반환한다() {
        // given
        ProductInfo productInfo = new ProductInfo(1L, 1L, "운동화", 50000, 8, ZonedDateTime.now(), ZonedDateTime.now());

        CreateOrderCommand command = new CreateOrderCommand(1L, 10L, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 2)
        ));

        given(productService.deductStock(1L, 2)).willReturn(productInfo);
        given(couponService.useCoupon(10L, 1L, 100000)).willReturn(5000);

        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo expectedInfo = new OrderInfo(1L, 1L, 10L, "COMPLETED", 100000, 5000, 95000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.createOrder(eq(1L), eq(10L), anyList(), eq(5000))).willReturn(expectedInfo);

        // when
        OrderInfo result = orderFacade.createOrder(command);

        // then
        assertThat(result.discountAmount()).isEqualTo(5000);
        assertThat(result.finalAmount()).isEqualTo(95000);
        verify(couponService).useCoupon(10L, 1L, 100000);
    }

    @Test
    void 쿠폰_검증에_실패하면_주문이_생성되지_않는다() {
        // given
        ProductInfo productInfo = new ProductInfo(1L, 1L, "운동화", 50000, 8, ZonedDateTime.now(), ZonedDateTime.now());

        CreateOrderCommand command = new CreateOrderCommand(1L, 10L, List.of(
                new CreateOrderCommand.CreateOrderItemCommand(1L, 2)
        ));

        given(productService.deductStock(1L, 2)).willReturn(productInfo);
        willThrow(new CoreException(ErrorType.FORBIDDEN, "본인 소유의 쿠폰만 사용할 수 있습니다."))
                .given(couponService).useCoupon(10L, 1L, 100000);

        // when & then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.FORBIDDEN));

        verify(orderService, never()).createOrder(any(), any(), anyList(), anyInt());
    }

    // --- 쿠폰 적용 주문 취소 테스트 ---

    @Test
    void 쿠폰이_적용된_주문을_취소하면_쿠폰이_복원된다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo cancelledInfo = new OrderInfo(1L, 1L, 10L, "CANCELLED", 100000, 5000, 95000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledInfo);

        // when
        OrderInfo result = orderFacade.cancelOrder(1L, 1L);

        // then
        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(productService).restoreStock(1L, 2);
        verify(couponService).restoreCoupon(10L);
    }

    @Test
    void 쿠폰_미적용_주문을_취소하면_쿠폰_복원이_호출되지_않는다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        OrderInfo cancelledInfo = new OrderInfo(1L, 1L, null, "CANCELLED", 100000, 0, 100000, List.of(
                new OrderInfo.OrderItemInfo(1L, 1L, "운동화", 50000, 2, 100000, now, now)
        ), now, now);
        given(orderService.cancelOrder(1L, 1L)).willReturn(cancelledInfo);

        // when
        orderFacade.cancelOrder(1L, 1L);

        // then
        verify(couponService, never()).restoreCoupon(any());
    }
}
