package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 주문을_생성하면_저장된_주문의_OrderInfo를_반환한다() {
        // given
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(1L, "운동화", 50000, 2)
        );
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderInfo result = orderService.createOrder(1L, items);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(100000);
        assertThat(result.orderItems()).hasSize(1);
        assertThat(result.orderItems().getFirst().productName()).isEqualTo("운동화");
    }

    @Test
    void 여러_상품을_주문하면_모든_주문항목이_포함된_OrderInfo를_반환한다() {
        // given
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(1L, "운동화", 50000, 2),
                new OrderItemCommand(2L, "슬리퍼", 30000, 1)
        );
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderInfo result = orderService.createOrder(1L, items);

        // then
        assertThat(result.totalPrice()).isEqualTo(130000);
        assertThat(result.orderItems()).hasSize(2);
        assertThat(result.orderItems().get(0).productName()).isEqualTo("운동화");
        assertThat(result.orderItems().get(1).productName()).isEqualTo("슬리퍼");
    }

    @Test
    void 존재하는_주문ID로_조회하면_Order를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        Order result = orderService.findOrder(1L);

        // then
        assertThat(result).isEqualTo(order);
    }

    @Test
    void 존재하지_않는_주문ID로_조회하면_CoreException_NOT_FOUND가_발생한다() {
        // given
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.findOrder(999L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 관리자용_주문_상세_조회시_OrderInfo를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        order.addOrderItem(OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build());
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        OrderInfo result = orderService.getOrder(1L);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(100000);
        assertThat(result.orderItems()).hasSize(1);
    }

    @Test
    void 관리자용_주문_상세_조회시_존재하지_않는_주문이면_NOT_FOUND가_발생한다() {
        // given
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 일반_사용자용_주문_상세_조회시_본인_주문이면_OrderInfo를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        order.addOrderItem(OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build());
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        OrderInfo result = orderService.getOrder(1L, 1L);

        // then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(100000);
        assertThat(result.orderItems()).hasSize(1);
    }

    @Test
    void 일반_사용자용_주문_상세_조회시_본인_주문이_아니면_FORBIDDEN이_발생한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(2L, 1L))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;
                    assertThat(coreException.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
                });
    }

    @Test
    void 일반_사용자용_주문_상세_조회시_존재하지_않는_주문이면_NOT_FOUND가_발생한다() {
        // given
        given(orderRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(1L, 999L))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 주문_목록을_조회하면_OrderInfo_페이지를_반환한다() {
        // given
        Order order = Order.builder().userId(1L).build();
        order.addOrderItem(OrderItem.builder()
                .productId(1L)
                .productName("운동화")
                .productPrice(50000)
                .quantity(2)
                .build());

        Pageable pageable = PageRequest.of(0, 20);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
        given(orderRepository.findAllByUserId(1L, startDate, endDate, pageable)).willReturn(orderPage);

        // when
        Page<OrderInfo> result = orderService.getOrders(1L, startDate, endDate, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().userId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().totalPrice()).isEqualTo(100000);
        assertThat(result.getContent().getFirst().orderItems()).hasSize(1);
        assertThat(result.getContent().getFirst().orderItems().getFirst().productName()).isEqualTo("운동화");
    }

    @Test
    void 주문이_없으면_빈_페이지를_반환한다() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        given(orderRepository.findAllByUserId(1L, startDate, endDate, pageable)).willReturn(emptyPage);

        // when
        Page<OrderInfo> result = orderService.getOrders(1L, startDate, endDate, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
