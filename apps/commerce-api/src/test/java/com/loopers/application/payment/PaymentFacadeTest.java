package com.loopers.application.payment;

import com.loopers.application.payment.command.PaymentCallbackCommand;
import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.PaymentGatewayResponse;
import com.loopers.domain.payment.PaymentGatewayClient.PgResponseStatus;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentRejectedEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentFacade paymentFacade;

    // --- requestPayment 테스트 ---

    @Test
    void PG_응답이_PENDING이면_transactionKey를_할당한다() {
        // given
        PaymentCreateCommand command = new PaymentCreateCommand(1L, 1L, "SAMSUNG", "1234-5678", 50000L);
        ZonedDateTime now = ZonedDateTime.now();
        PaymentResult createdResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, null, "PENDING", null, now, now);
        given(paymentService.requestPayment(command)).willReturn(createdResult);

        PaymentGatewayResponse pgResponse = new PaymentGatewayResponse("txn-key-123", PgResponseStatus.PENDING, null);
        given(paymentGatewayClient.requestPayment(any())).willReturn(pgResponse);

        PaymentResult completedResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, "txn-key-123", "PENDING", null, now, now);
        given(paymentService.completePaymentRequest(1L, "txn-key-123")).willReturn(completedResult);

        // when
        PaymentResult result = paymentFacade.requestPayment(command);

        // then
        assertThat(result.transactionKey()).isEqualTo("txn-key-123");
        verify(paymentService).completePaymentRequest(1L, "txn-key-123");
    }

    @Test
    void PG_응답이_REJECTED이면_결제를_실패_처리하고_예외를_발생시킨다() {
        // given
        PaymentCreateCommand command = new PaymentCreateCommand(1L, 1L, "SAMSUNG", "1234-5678", 50000L);
        ZonedDateTime now = ZonedDateTime.now();
        PaymentResult createdResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, null, "PENDING", null, now, now);
        given(paymentService.requestPayment(command)).willReturn(createdResult);

        PaymentGatewayResponse pgResponse = new PaymentGatewayResponse(null, PgResponseStatus.REJECTED, "잔액 부족");
        given(paymentGatewayClient.requestPayment(any())).willReturn(pgResponse);

        // when & then
        assertThatThrownBy(() -> paymentFacade.requestPayment(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.PAYMENT_FAILED));

        verify(paymentService).failPaymentApproval(1L, "잔액 부족");
    }

    @Test
    void PG_타임아웃이면_결제를_UNKNOWN_처리하고_예외를_발생시킨다() {
        // given
        PaymentCreateCommand command = new PaymentCreateCommand(1L, 1L, "SAMSUNG", "1234-5678", 50000L);
        ZonedDateTime now = ZonedDateTime.now();
        PaymentResult createdResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, null, "PENDING", null, now, now);
        given(paymentService.requestPayment(command)).willReturn(createdResult);

        willThrow(new CoreException(ErrorType.PAYMENT_GATEWAY_TIMEOUT, "PG 응답 시간 초과"))
                .given(paymentGatewayClient).requestPayment(any());

        // when & then
        assertThatThrownBy(() -> paymentFacade.requestPayment(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.PAYMENT_GATEWAY_TIMEOUT));

        verify(paymentService).suspendPaymentByTimeout(1L);
    }

    @Test
    void PG_에러이면_결제를_실패_처리하고_예외를_발생시킨다() {
        // given
        PaymentCreateCommand command = new PaymentCreateCommand(1L, 1L, "SAMSUNG", "1234-5678", 50000L);
        ZonedDateTime now = ZonedDateTime.now();
        PaymentResult createdResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, null, "PENDING", null, now, now);
        given(paymentService.requestPayment(command)).willReturn(createdResult);

        willThrow(new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "PG 서버 오류"))
                .given(paymentGatewayClient).requestPayment(any());

        // when & then
        assertThatThrownBy(() -> paymentFacade.requestPayment(command))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.PAYMENT_GATEWAY_ERROR));

        verify(paymentService).failPaymentApproval(1L, "PG 서버 오류");
    }

    // --- handleCallback 테스트 ---

    @Test
    void 결제_승인_콜백을_받으면_승인_이벤트를_발행한다() {
        // given
        PaymentCallbackCommand command = new PaymentCallbackCommand("txn-key-123", "1", "SAMSUNG", "1234-5678", 50000L, PaymentStatus.APPROVED, null);
        ZonedDateTime now = ZonedDateTime.now();
        PaymentResult approvedResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, "txn-key-123", "APPROVED", null, now, now);
        given(paymentService.handleCallback(command)).willReturn(approvedResult);

        // when
        paymentFacade.handleCallback(command);

        // then
        ArgumentCaptor<PaymentApprovedEvent> captor = ArgumentCaptor.forClass(PaymentApprovedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(1L);
    }

    @Test
    void 결제_거절_콜백을_받으면_거절_이벤트를_발행한다() {
        // given
        PaymentCallbackCommand command = new PaymentCallbackCommand("txn-key-123", "1", "SAMSUNG", "1234-5678", 50000L, PaymentStatus.REJECTED, "카드 한도 초과");
        ZonedDateTime now = ZonedDateTime.now();
        PaymentResult rejectedResult = new PaymentResult(1L, 1L, 1L, "SAMSUNG", "1234-5678", 50000L, "txn-key-123", "REJECTED", "카드 한도 초과", now, now);
        given(paymentService.handleCallback(command)).willReturn(rejectedResult);

        // when
        paymentFacade.handleCallback(command);

        // then
        ArgumentCaptor<PaymentRejectedEvent> captor = ArgumentCaptor.forClass(PaymentRejectedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PaymentRejectedEvent event = captor.getValue();
        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.userId()).isEqualTo(1L);
    }
}
