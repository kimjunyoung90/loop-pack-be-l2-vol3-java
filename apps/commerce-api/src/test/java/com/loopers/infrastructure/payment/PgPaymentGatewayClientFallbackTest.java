package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.PaymentGatewayRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
        "pg.base-url=http://localhost:59999",
        "pg.connect-timeout=1000",
        "pg.read-timeout=1000",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback"
})
class PgPaymentGatewayClientFallbackTest {

    @Autowired
    private PaymentGatewayClient paymentGatewayClient;

    @MockitoBean
    private RestClient pgRestClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private final PaymentGatewayRequest request = new PaymentGatewayRequest(
            1L, "ORDER-001", "VISA", "4111111111111111", 10000L, "http://callback"
    );

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("paymentGateway").reset();
    }

    @Test
    void PG_서버_오류_시_RestClientException_fallback이_실행된다() {
        // given - RestClient가 RestClientException을 던지도록 설정
        given(pgRestClient.post()).willThrow(new RestClientException("PG 서버 오류"));

        // when & then
        assertThatThrownBy(() -> paymentGatewayClient.requestPayment(request))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> {
                    CoreException ce = (CoreException) e;
                    assertThat(ce.getErrorType()).isEqualTo(ErrorType.PAYMENT_GATEWAY_ERROR);
                    assertThat(ce.getCustomMessage()).isNull();
                });
    }
}
