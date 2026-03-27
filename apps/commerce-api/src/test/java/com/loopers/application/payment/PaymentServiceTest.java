package com.loopers.application.payment;

import com.loopers.application.payment.command.PaymentCallbackCommand;
import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    // --- requestPayment ---

    @Test
    void 결제를_요청하면_PENDING_상태로_생성된다() {
        // given
        PaymentCreateCommand command = new PaymentCreateCommand(1L, 1L, "SAMSUNG", "1234-5678", 50000L);

        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PaymentResult result = paymentService.requestPayment(command);

        // then
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.cardType()).isEqualTo("SAMSUNG");
        assertThat(result.amount()).isEqualTo(50000L);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    // --- completePaymentRequest ---

    @Test
    void transactionKey를_할당하면_결과에_반영된다() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L).userId(1L).cardType(CardType.SAMSUNG).cardNo("1234-5678").amount(50000L)
                .build();
        given(paymentRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(payment));

        // when
        PaymentResult result = paymentService.completePaymentRequest(1L, "txn-key-123");

        // then
        assertThat(result.transactionKey()).isEqualTo("txn-key-123");
    }

    @Test
    void transactionKey_할당_시_결제가_존재하지_않으면_예외가_발생한다() {
        // given
        given(paymentRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.completePaymentRequest(999L, "txn-key"))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    // --- failPaymentApproval ---

    @Test
    void 결제_승인을_실패_처리하면_REJECTED_상태가_된다() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L).userId(1L).cardType(CardType.SAMSUNG).cardNo("1234-5678").amount(50000L)
                .build();
        given(paymentRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(payment));

        // when
        PaymentResult result = paymentService.failPaymentApproval(1L, "잔액 부족");

        // then
        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.failureReason()).isEqualTo("잔액 부족");
    }

    @Test
    void 결제_실패_처리_시_결제가_존재하지_않으면_예외가_발생한다() {
        // given
        given(paymentRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.failPaymentApproval(999L, "사유"))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }

    // --- suspendPaymentByTimeout ---

    @Test
    void 타임아웃이면_UNKNOWN_상태가_된다() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L).userId(1L).cardType(CardType.SAMSUNG).cardNo("1234-5678").amount(50000L)
                .build();
        given(paymentRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(payment));

        // when
        PaymentResult result = paymentService.suspendPaymentByTimeout(1L);

        // then
        assertThat(result.status()).isEqualTo("UNKNOWN");
    }

    // --- handleCallback ---

    @Test
    void 승인_콜백을_처리하면_APPROVED_상태가_된다() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L).userId(1L).cardType(CardType.SAMSUNG).cardNo("1234-5678").amount(50000L)
                .build();
        payment.assignTransactionKey("txn-key-123");
        given(paymentRepository.findByTransactionKeyAndDeletedAtIsNull("txn-key-123")).willReturn(Optional.of(payment));

        PaymentCallbackCommand command = new PaymentCallbackCommand("txn-key-123", "1", "SAMSUNG", "1234-5678", 50000L, PaymentStatus.APPROVED, null);

        // when
        PaymentResult result = paymentService.handleCallback(command);

        // then
        assertThat(result.status()).isEqualTo("APPROVED");
    }

    @Test
    void 거절_콜백을_처리하면_REJECTED_상태와_사유가_설정된다() {
        // given
        Payment payment = Payment.builder()
                .orderId(1L).userId(1L).cardType(CardType.SAMSUNG).cardNo("1234-5678").amount(50000L)
                .build();
        payment.assignTransactionKey("txn-key-123");
        given(paymentRepository.findByTransactionKeyAndDeletedAtIsNull("txn-key-123")).willReturn(Optional.of(payment));

        PaymentCallbackCommand command = new PaymentCallbackCommand("txn-key-123", "1", "SAMSUNG", "1234-5678", 50000L, PaymentStatus.REJECTED, "카드 한도 초과");

        // when
        PaymentResult result = paymentService.handleCallback(command);

        // then
        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.failureReason()).isEqualTo("카드 한도 초과");
    }

    @Test
    void 콜백_처리_시_transactionKey가_존재하지_않으면_예외가_발생한다() {
        // given
        given(paymentRepository.findByTransactionKeyAndDeletedAtIsNull("unknown-key")).willReturn(Optional.empty());

        PaymentCallbackCommand command = new PaymentCallbackCommand("unknown-key", "1", "SAMSUNG", "1234-5678", 50000L, PaymentStatus.APPROVED, null);

        // when & then
        assertThatThrownBy(() -> paymentService.handleCallback(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.NOT_FOUND));
    }
}
