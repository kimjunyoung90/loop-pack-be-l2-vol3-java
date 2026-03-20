package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.PaymentGatewayRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "resilience4j.circuitbreaker.instances.paymentGateway.sliding-window-size=5",
        "resilience4j.circuitbreaker.instances.paymentGateway.minimum-number-of-calls=3",
        "resilience4j.circuitbreaker.instances.paymentGateway.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.paymentGateway.wait-duration-in-open-state=10s",
        "pg.base-url=http://localhost:59999",
        "pg.connect-timeout=1000",
        "pg.read-timeout=1000",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback"
})
class PgPaymentGatewayClientIntegrationTest {

    @Autowired
    private PaymentGatewayClient paymentGatewayClient;

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
    void 연결_실패_시_ResourceAccessException_fallback이_실행된다() {
        // when - 존재하지 않는 서버로 요청 → ResourceAccessException 발생
        // then - ResourceAccessException fallback → "TIMEOUT" 메시지
        assertThatThrownBy(() -> paymentGatewayClient.requestPayment(request))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> {
                    CoreException ce = (CoreException) e;
                    assertThat(ce.getErrorType()).isEqualTo(ErrorType.PAYMENT_GATEWAY_ERROR);
                    assertThat(ce.getCustomMessage()).isEqualTo("TIMEOUT");
                });
    }

    @Test
    void 실패가_임계치를_넘으면_서킷이_OPEN_상태로_전환된다() {
        // given - 3회 실패 (minimum-number-of-calls = 3, failure-rate-threshold = 50%)
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> paymentGatewayClient.requestPayment(request))
                    .isInstanceOf(CoreException.class);
        }

        // when & then - 서킷이 OPEN 상태로 전환
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentGateway");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void 서킷이_OPEN_상태이면_CallNotPermittedException_fallback이_실행된다() {
        // given - 서킷을 OPEN 상태로 강제 전환
        circuitBreakerRegistry.circuitBreaker("paymentGateway").transitionToOpenState();

        // when & then - OPEN 상태에서 호출 → CallNotPermittedException fallback
        assertThatThrownBy(() -> paymentGatewayClient.requestPayment(request))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> {
                    CoreException ce = (CoreException) e;
                    assertThat(ce.getErrorType()).isEqualTo(ErrorType.PAYMENT_GATEWAY_ERROR);
                    assertThat(ce.getCustomMessage()).isEqualTo("서킷 브레이커가 OPEN 상태입니다.");
                });
    }
}
