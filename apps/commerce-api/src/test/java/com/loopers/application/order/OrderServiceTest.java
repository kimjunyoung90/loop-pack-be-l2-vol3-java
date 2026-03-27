package com.loopers.application.order;

import com.loopers.application.order.result.OrderResult;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderService orderService;

	@Test
	void 관리자용_주문_상세_조회_시_존재하지_않는_주문이면_예외가_발생한다() {
		// given
		given(orderRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.getOrder(999L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
	}

	@Test
	void 일반_사용자용_주문_상세_조회_시_본인_주문이_아니면_예외가_발생한다() {
		// given
		Order order = Order.builder().userId(1L).build();
		given(orderRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> orderService.getOrder(2L, 1L))
				.isInstanceOf(CoreException.class)
				.satisfies(exception -> {
					CoreException coreException = (CoreException) exception;
					assertThat(coreException.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
				});
	}

	@Test
	void 일반_사용자용_주문_상세_조회_시_존재하지_않는_주문이면_예외가_발생한다() {
		// given
		given(orderRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.getOrder(1L, 999L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
	}

	// --- completeOrder ---

	@Test
	void 주문을_완료하면_COMPLETED_상태의_결과를_반환한다() {
		// given
		Order order = Order.builder().userId(1L).build();
		given(orderRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(order));

		// when
		OrderResult result = orderService.completeOrder(1L);

		// then
		assertThat(result.status()).isEqualTo("COMPLETED");
	}

	@Test
	void 주문_완료_시_존재하지_않는_주문이면_예외가_발생한다() {
		// given
		given(orderRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.completeOrder(999L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
	}

	@Test
	void 이미_완료된_주문을_다시_완료하면_예외가_발생한다() {
		// given
		Order order = Order.builder().userId(1L).build();
		order.complete();
		given(orderRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> orderService.completeOrder(1L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
	}

	// --- cancelOrder ---

	@Test
	void 주문을_취소하면_CANCELLED_상태의_결과를_반환한다() {
		// given
		Order order = Order.builder().userId(1L).build();
		given(orderRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(order));

		// when
		OrderResult result = orderService.cancelOrder(1L, 1L);

		// then
		assertThat(result.status()).isEqualTo("CANCELLED");
	}

	@Test
	void 주문_취소_시_존재하지_않는_주문이면_예외가_발생한다() {
		// given
		given(orderRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(1L, 999L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
	}

	@Test
	void 본인_주문이_아니면_취소_시_예외가_발생한다() {
		// given
		Order order = Order.builder().userId(1L).build();
		given(orderRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(999L, 1L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.FORBIDDEN));
	}

	@Test
	void 이미_취소된_주문을_다시_취소하면_예외가_발생한다() {
		// given
		Order order = Order.builder().userId(1L).build();
		order.cancel(1L);
		given(orderRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
	}
}
