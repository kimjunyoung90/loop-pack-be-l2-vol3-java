package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.PgConnectException;
import com.loopers.infrastructure.payment.dto.PgApiResponse;
import com.loopers.infrastructure.payment.dto.PgOrderResponse;
import com.loopers.infrastructure.payment.dto.PgPaymentRequest;
import com.loopers.infrastructure.payment.dto.PgPaymentResponse;
import com.loopers.infrastructure.payment.dto.PgTransactionResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PgPaymentGatewayClient implements PaymentGatewayClient {

    private final RestClient pgRestClient;

    @Override
    @Retry(name = "paymentGateway")
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "requestPaymentFallback")
    public PaymentGatewayResponse requestPayment(PaymentGatewayRequest request) {
        PgPaymentRequest pgRequest = new PgPaymentRequest(
                request.orderId(),
                request.cardType(),
                request.cardNo(),
                request.amount(),
                request.callbackUrl()
        );

        try {
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
        } catch (ResourceAccessException e) {
            // 도달 여부를 원인(cause)으로 구분 — 도달 전 실패만 Retry 대상
            Throwable cause = e.getCause();
            if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
                throw new PgConnectException(cause);
            }
            // Read Timeout 등은 도달했을 수 있으므로 Retry 금지 → 그대로 전파
            throw e;
        }
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

    // PgConnectException용 fallback은 의도적으로 없음 — Retry가 재시도할 수 있도록 CB를 통과시켜야 함.
    // Retry 소진 후 PgConnectException은 PaymentFacade로 전파되어 REJECTED로 처리됨.

    //서킷 오픈
    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, CallNotPermittedException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR, "서킷 브레이커가 OPEN 상태입니다.");
    }

    //Read Timeout 등 응답 미수신 (도달했을 수 있음 → UNKNOWN)
    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, ResourceAccessException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_TIMEOUT);
    }

    //예상치 못한 RestClient 오류(최종 방어선)
    private PaymentGatewayResponse requestPaymentFallback(PaymentGatewayRequest request, RestClientException e) {
        throw new CoreException(ErrorType.PAYMENT_GATEWAY_ERROR);
    }
}
