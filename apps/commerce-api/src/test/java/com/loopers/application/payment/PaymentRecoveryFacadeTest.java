package com.loopers.application.payment;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.ReconciliationResult;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentRecoveryFacadeTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentRecoveryFacade paymentRecoveryFacade;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentRecoveryFacade, "pendingGraceSeconds", 10L);
        ReflectionTestUtils.setField(paymentRecoveryFacade, "batchSize", 50);
    }

    private Payment mockPayment(Long id, Long userId, Long orderId) {
        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .cardType(CardType.SAMSUNG)
                .cardNo("1234-5678-9012-3456")
                .amount(10000L)
                .build();
        ReflectionTestUtils.setField(payment, "id", id);
        return payment;
    }

    @Test
    @DisplayName("대상이 없으면 PG 조회를 시도하지 않는다")
    void 복구_대상이_없으면_아무것도_하지_않는다() {
        // given
        given(paymentRepository.findRecoveryTargets(any(ZonedDateTime.class), any(Pageable.class)))
                .willReturn(List.of());

        // when
        paymentRecoveryFacade.reconcilePendingPayments();

        // then
        verifyNoInteractions(paymentGatewayClient);
        verifyNoInteractions(paymentService);
    }

    @Test
    @DisplayName("PG 응답이 APPROVED면 reconcileByResult가 호출된다")
    void PG_응답이_APPROVED면_결제를_확정한다() {
        // given
        Payment target = mockPayment(1L, 100L, 200L);
        ReconciliationResult result = new ReconciliationResult("tx-key-1", PaymentStatus.APPROVED, null);

        given(paymentRepository.findRecoveryTargets(any(ZonedDateTime.class), any(Pageable.class)))
                .willReturn(List.of(target));
        given(paymentGatewayClient.findByOrderId(100L, 200L)).willReturn(Optional.of(result));

        // when
        paymentRecoveryFacade.reconcilePendingPayments();

        // then
        verify(paymentService).reconcileByResult(1L, result);
        verify(paymentService, never()).reconcileAsNotFound(anyLong());
    }

    @Test
    @DisplayName("PG 응답이 REJECTED면 reconcileByResult가 호출된다")
    void PG_응답이_REJECTED면_결제를_거절한다() {
        // given
        Payment target = mockPayment(2L, 100L, 200L);
        ReconciliationResult result = new ReconciliationResult("tx-key-2", PaymentStatus.REJECTED, "한도 초과");

        given(paymentRepository.findRecoveryTargets(any(ZonedDateTime.class), any(Pageable.class)))
                .willReturn(List.of(target));
        given(paymentGatewayClient.findByOrderId(100L, 200L)).willReturn(Optional.of(result));

        // when
        paymentRecoveryFacade.reconcilePendingPayments();

        // then
        ArgumentCaptor<ReconciliationResult> captor = ArgumentCaptor.forClass(ReconciliationResult.class);
        verify(paymentService).reconcileByResult(eq(2L), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(captor.getValue().reason()).isEqualTo("한도 초과");
    }

    @Test
    @DisplayName("PG에서 찾을 수 없으면 reconcileAsNotFound가 호출된다")
    void PG에서_찾을_수_없으면_도달_실패로_확정한다() {
        // given
        Payment target = mockPayment(3L, 100L, 200L);

        given(paymentRepository.findRecoveryTargets(any(ZonedDateTime.class), any(Pageable.class)))
                .willReturn(List.of(target));
        given(paymentGatewayClient.findByOrderId(100L, 200L)).willReturn(Optional.empty());

        // when
        paymentRecoveryFacade.reconcilePendingPayments();

        // then
        verify(paymentService).reconcileAsNotFound(3L);
        verify(paymentService, never()).reconcileByResult(anyLong(), any());
    }

    @Test
    @DisplayName("PG 응답이 PENDING이면 상태 변경 없이 다음 주기를 기다린다")
    void PG_응답이_PENDING이면_변경하지_않고_대기한다() {
        // given
        Payment target = mockPayment(4L, 100L, 200L);
        ReconciliationResult result = new ReconciliationResult("tx-key-4", PaymentStatus.PENDING, null);

        given(paymentRepository.findRecoveryTargets(any(ZonedDateTime.class), any(Pageable.class)))
                .willReturn(List.of(target));
        given(paymentGatewayClient.findByOrderId(100L, 200L)).willReturn(Optional.of(result));

        // when
        paymentRecoveryFacade.reconcilePendingPayments();

        // then — reconcileByResult는 호출되지만 PaymentService 내부에서 no-op
        verify(paymentService).reconcileByResult(4L, result);
        verify(paymentService, never()).reconcileAsNotFound(anyLong());
    }

    @Test
    @DisplayName("한 건의 PG 조회 실패가 다른 결제의 복구를 막지 않는다")
    void 한_건_실패는_다른_결제_처리를_막지_않는다() {
        // given
        Payment target1 = mockPayment(10L, 100L, 201L);
        Payment target2 = mockPayment(11L, 100L, 202L);
        ReconciliationResult ok = new ReconciliationResult("tx-ok", PaymentStatus.APPROVED, null);

        given(paymentRepository.findRecoveryTargets(any(ZonedDateTime.class), any(Pageable.class)))
                .willReturn(List.of(target1, target2));
        willThrow(new RuntimeException("PG 호출 실패"))
                .given(paymentGatewayClient).findByOrderId(100L, 201L);
        given(paymentGatewayClient.findByOrderId(100L, 202L)).willReturn(Optional.of(ok));

        // when
        paymentRecoveryFacade.reconcilePendingPayments();

        // then — 2번째 결제는 정상 처리되어야 함
        verify(paymentService).reconcileByResult(11L, ok);
        verify(paymentService, never()).reconcileByResult(eq(10L), any());
    }
}
