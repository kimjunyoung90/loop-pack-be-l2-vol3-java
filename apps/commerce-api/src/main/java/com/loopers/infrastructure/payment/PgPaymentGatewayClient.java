package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.infrastructure.payment.dto.PgApiResponse;
import com.loopers.infrastructure.payment.dto.PgOrderResponse;
import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

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
        PgResponseStatus status = PgResponseStatus.valueOf(data.status());
        return new PaymentGatewayResponse(data.transactionKey(), status, data.reason());
    }

    @Override
    public Optional<ReconciliationResult> findByOrderId(Long userId, Long orderId) {
        try {
            PgApiResponse<PgOrderResponse> response = pgRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/payments")
                            .queryParam("orderId", orderId)
                            .build())
                    .header("X-USER-ID", String.valueOf(userId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || response.data() == null || response.data().transactions() == null
                    || response.data().transactions().isEmpty()) {
                return Optional.empty();
            }

            PgTransactionResponse tx = response.data().transactions().get(0);
            return Optional.of(new ReconciliationResult(
                    tx.transactionKey(),
                    mapToPaymentStatus(tx.status()),
                    tx.reason()
            ));
        } catch (HttpClientErrorException.NotFound e) {
            // PG에 해당 orderId가 존재하지 않음 → 실제로 도달 안 함
            return Optional.empty();
        }
    }

    private PaymentStatus mapToPaymentStatus(String pgStatus) {
        return switch (pgStatus) {
            case "SUCCESS" -> PaymentStatus.APPROVED;
            case "FAILED" -> PaymentStatus.REJECTED;
            case "PENDING" -> PaymentStatus.PENDING;
            default -> PaymentStatus.UNKNOWN;
        };
    }

    //서킷 오픈
    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, CallNotPermittedException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "서킷 브레이커가 OPEN 상태입니다.");
    }

    //타임 아웃
    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, ResourceAccessException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_TIMEOUT);
    }

    //예상치 못한 RestClient 오류(최종 방어선)
    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, RestClientException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR);
    }
}
