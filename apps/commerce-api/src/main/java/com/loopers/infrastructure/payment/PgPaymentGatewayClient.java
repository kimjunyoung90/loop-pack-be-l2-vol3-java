package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PgApiResponse;
import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
@Component
public class PgPaymentGatewayClient implements PaymentGatewayClient {

    private final RestClient pgRestClient;

    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "requestPaymentFallback")
    public PaymentGatewayResponse requestPayment(PaymentGatewayRequest request) {
        PgPaymentRequest pgRequest = new PgPaymentRequest(
                request.orderId(),
                request.cardType(),
                request.cardNo(),
                request.amount(),
                request.callbackUrl()
        );

        PgApiResponse<PgPaymentResponse> response = pgRestClient.post()
                .uri("/api/v1/payments")
                .header("X-USER-ID", String.valueOf(request.userId()))
                .body(pgRequest)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.data() == null) {
            throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "PG 응답이 비어있습니다.");
        }

        PgPaymentResponse data = response.data();
        return new PaymentGatewayResponse(data.transactionKey(), data.status(), data.reason());
    }

    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, CallNotPermittedException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "서킷 브레이커가 OPEN 상태입니다.");
    }

    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, ResourceAccessException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "TIMEOUT");
    }

    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, RestClientException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR);
    }
}
