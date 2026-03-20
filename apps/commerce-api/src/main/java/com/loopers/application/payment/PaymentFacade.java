package com.loopers.application.payment;

import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.PaymentGatewayRequest;
import com.loopers.domain.payment.PaymentGatewayClient.PaymentGatewayResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentGatewayClient paymentGatewayClient;

    @Value("${pg.callback-url}")
    private String callbackUrl;

    public PaymentResult requestPayment(PaymentCreateCommand command) {
        // TX1: Payment 생성 (PENDING) → 커밋
        Payment payment = paymentService.createPendingPayment(command);

        // 트랜잭션 밖: PG 호출
        try {
            PaymentGatewayResponse response = paymentGatewayClient.requestPayment(
                    new PaymentGatewayRequest(
                            command.userId(),
                            String.valueOf(command.orderId()),
                            command.cardType(),
                            command.cardNo(),
                            command.amount(),
                            callbackUrl
                    )
            );

            if ("PENDING".equals(response.status())) {
                // TX2: transactionKey 할당 → 커밋
                return paymentService.completePaymentRequest(payment.getId(), response.transactionKey());
            } else {
                // TX2: reject → 커밋
                paymentService.failPayment(payment.getId(), response.reason());
                throw new CoreException(ErrorType.PAYMENT_FAILED, response.reason());
            }
        } catch (CoreException e) {
            if (e.getErrorType() == ErrorType.PAYMENT_GATEWAY_ERROR) {
                if ("TIMEOUT".equals(e.getCustomMessage())) {
                    // TX2: unknown → 커밋
                    paymentService.unknownPayment(payment.getId());
                } else {
                    // TX2: reject → 커밋
                    paymentService.failPayment(payment.getId(), e.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            paymentService.unknownPayment(payment.getId());
            throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "결제 요청 중 알 수 없는 오류가 발생했습니다.");
        }
    }
}
