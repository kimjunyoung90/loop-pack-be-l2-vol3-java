package com.loopers.application.payment;

import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.PgResponseStatus;
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
        //1. Payment 생성(TX1)
        PaymentResult result = paymentService.requestPayment(command);
        Long paymentId = result.id();

        //2. PG 호출(트랜잭션 밖)
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

			//3. PG 응답(TX2)
            if (response.status() == PgResponseStatus.PENDING) {
                // TX2: transactionKey 할당 → 커밋
                return paymentService.completePaymentRequest(paymentId, response.transactionKey());
            } else {
                // TX2: reject → 커밋
                paymentService.failPaymentApproval(paymentId, response.reason());
                throw new CoreException(ErrorType.PAYMENT_FAILED, response.reason());
            }
        } catch (CoreException e) {
            if (e.getErrorType() == ErrorType.PAYMENT_GATEWAY_TIMEOUT) {
                // TX2: unknown → 커밋
                paymentService.suspendPaymentByTimeout(paymentId);
            } else if (e.getErrorType() == ErrorType.PAYMENT_GATEWAY_ERROR) {
                // TX2: reject → 커밋
                paymentService.failPaymentApproval(paymentId, e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            paymentService.suspendPaymentByTimeout(paymentId);
            throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "결제 요청 중 알 수 없는 오류가 발생했습니다.");
        }
    }
}
