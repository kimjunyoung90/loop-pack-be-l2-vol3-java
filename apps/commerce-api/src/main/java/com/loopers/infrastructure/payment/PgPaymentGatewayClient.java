package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.infrastructure.payment.dto.PgApiResponse;
import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public PaymentGatewayResponse requestPayment(PaymentGatewayRequest request) {
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentGateway");

        try {
            PgApiResponse<PgPaymentResponse> response = circuitBreaker.executeSupplier(() -> {
                PgPaymentRequest pgRequest = new PgPaymentRequest(
                        request.orderId(),
                        request.cardType(),
                        request.cardNo(),
                        request.amount(),
                        request.callbackUrl()
                );

                return pgRestClient.post()
                        .uri("/api/v1/payments")
                        .header("X-USER-ID", String.valueOf(request.userId()))
                        .body(pgRequest)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});
            });

            if (response == null || response.data() == null) {
                throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "PG 응답이 비어있습니다.");
            }

            PgPaymentResponse data = response.data();
            return new PaymentGatewayResponse(data.transactionKey(), data.status(), data.reason());

        } catch (CallNotPermittedException e) {
            throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "서킷 브레이커가 OPEN 상태입니다.");
        } catch (ResourceAccessException e) {
            throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "TIMEOUT");
        } catch (RestClientException e) {
            throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR);
        }
    }
}
