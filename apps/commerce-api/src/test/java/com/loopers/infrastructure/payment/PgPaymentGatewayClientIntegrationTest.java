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
        "resilience4j.retry.instances.paymentGateway.max-attempts=1",  // 테스트에서 재시도 비활성
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
    void 연결_실패_시_Retry_소진_후_PgConnectException이_전파된다() {
        // when - 존재하지 않는 서버로 요청 → ConnectException → PgConnectException → Retry 3회 후 전파
        // then - PgConnectException이 그대로 전파되어 PaymentFacade에서 REJECTED 처리되도록 함
        assertThatThrownBy(() -> paymentGatewayClient.requestPayment(request))
                .isInstanceOf(com.loopers.domain.payment.PgConnectException.class);
    }

    @Test
    void 연결_실패가_임계치를_넘으면_서킷이_OPEN_상태로_전환된다() {
        // given - 연결 실패 3회 (Retry는 max-attempts=1로 비활성)
        //        record-exceptions에 PgConnectException 포함 → CB가 각 실패를 기록
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> paymentGatewayClient.requestPayment(request))
                    .isInstanceOf(com.loopers.domain.payment.PgConnectException.class);
        }

        // then - 3회 실패 기록 → 서킷 OPEN (장기 PG 장애 시 빠른 차단)
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
