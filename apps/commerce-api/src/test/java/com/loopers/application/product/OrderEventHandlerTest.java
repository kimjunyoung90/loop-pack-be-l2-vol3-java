package com.loopers.application.product;

import com.loopers.application.product.result.ProductResult;
import com.loopers.domain.order.event.OrderCancelledEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderEventHandler handler;

    @Test
    void 주문_생성_이벤트를_수신하면_재고를_차감한다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(productService.deductStock(1L, 2))
                .willReturn(new ProductResult(1L, 1L, "운동화", 50000, 8, now, now));

        OrderPlacedEvent event = new OrderPlacedEvent(List.of(
                new OrderPlacedEvent.ItemStock(1L, 2)
        ), null, 1L, 100000);

        // when
        handler.handleOrderPlaced(event);

        // then
        verify(productService).deductStock(1L, 2);
    }

    @Test
    void 주문_생성_이벤트_수신_시_productId_오름차순으로_재고를_차감한다() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        given(productService.deductStock(1L, 1))
                .willReturn(new ProductResult(1L, 1L, "운동화", 50000, 9, now, now));
        given(productService.deductStock(2L, 3))
                .willReturn(new ProductResult(2L, 1L, "티셔츠", 30000, 7, now, now));

        OrderPlacedEvent event = new OrderPlacedEvent(List.of(
                new OrderPlacedEvent.ItemStock(2L, 3),
                new OrderPlacedEvent.ItemStock(1L, 1)
        ), null, 1L, 80000);

        // when
        handler.handleOrderPlaced(event);

        // then
        InOrder inOrder = inOrder(productService);
        inOrder.verify(productService).deductStock(1L, 1);
        inOrder.verify(productService).deductStock(2L, 3);
    }

    @Test
    void 주문_취소_이벤트를_수신하면_재고를_복원한다() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(null, List.of(
                new OrderCancelledEvent.ItemStock(1L, 2)
        ));

        // when
        handler.handleOrderCancelled(event);

        // then
        verify(productService).restoreStock(1L, 2);
    }

    @Test
    void 주문_취소_이벤트_수신_시_productId_오름차순으로_재고를_복원한다() {
        // given
        OrderCancelledEvent event = new OrderCancelledEvent(null, List.of(
                new OrderCancelledEvent.ItemStock(2L, 3),
                new OrderCancelledEvent.ItemStock(1L, 1)
        ));

        // when
        handler.handleOrderCancelled(event);

        // then
        InOrder inOrder = inOrder(productService);
        inOrder.verify(productService).restoreStock(1L, 1);
        inOrder.verify(productService).restoreStock(2L, 3);
    }
}
