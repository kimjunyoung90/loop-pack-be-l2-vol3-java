package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private Payment newPending() {
        return Payment.builder()
                .orderId(1L)
                .userId(1L)
                .cardType(CardType.SAMSUNG)
                .cardNo("1234-5678-9012-3456")
                .amount(10000L)
                .build();
    }

    @Nested
    @DisplayName("approve 멱등성")
    class ApproveIdempotency {

        @Test
        @DisplayName("PENDING에서 approve하면 APPROVED가 된다")
        void PENDING에서_승인하면_APPROVED가_된다() {
            Payment payment = newPending();
            payment.approve();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        }

        @Test
        @DisplayName("이미 APPROVED인 상태에서 approve하면 예외 없이 no-op")
        void 이미_APPROVED이면_멱등_no_op이다() {
            Payment payment = newPending();
            payment.approve();
            payment.approve();   // 두 번째 호출도 예외 없이 통과
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        }

        @Test
        @DisplayName("REJECTED 상태에서 approve하면 충돌 예외가 발생한다")
        void REJECTED에서_승인하면_예외() {
            Payment payment = newPending();
            payment.reject("한도 초과");
            assertThatThrownBy(payment::approve)
                    .isInstanceOf(CoreException.class)
                    .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.CONFLICT));
        }
    }

    @Nested
    @DisplayName("reject 멱등성")
    class RejectIdempotency {

        @Test
        @DisplayName("PENDING에서 reject하면 REJECTED가 된다")
        void PENDING에서_거절하면_REJECTED가_된다() {
            Payment payment = newPending();
            payment.reject("잔액 부족");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
            assertThat(payment.getFailureReason()).isEqualTo("잔액 부족");
        }

        @Test
        @DisplayName("이미 REJECTED인 상태에서 reject하면 예외 없이 no-op, reason도 덮어쓰지 않는다")
        void 이미_REJECTED이면_멱등_no_op이고_reason은_유지된다() {
            Payment payment = newPending();
            payment.reject("최초 사유");
            payment.reject("나중 사유");   // 덮어쓰지 않아야 함
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
            assertThat(payment.getFailureReason()).isEqualTo("최초 사유");
        }

        @Test
        @DisplayName("APPROVED 상태에서 reject하면 충돌 예외가 발생한다")
        void APPROVED에서_거절하면_예외() {
            Payment payment = newPending();
            payment.approve();
            assertThatThrownBy(() -> payment.reject("뒤늦은 거절"))
                    .isInstanceOf(CoreException.class)
                    .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.CONFLICT));
        }
    }

    @Nested
    @DisplayName("UNKNOWN 상태 전이")
    class UnknownTransition {

        @Test
        @DisplayName("UNKNOWN에서 approve하면 APPROVED로 전이된다")
        void UNKNOWN에서_APPROVED로_전이() {
            Payment payment = newPending();
            payment.unknown();
            payment.approve();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        }

        @Test
        @DisplayName("UNKNOWN에서 reject하면 REJECTED로 전이된다")
        void UNKNOWN에서_REJECTED로_전이() {
            Payment payment = newPending();
            payment.unknown();
            payment.reject("PG에서 확인 불가");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
            assertThat(payment.getFailureReason()).isEqualTo("PG에서 확인 불가");
        }
    }
}
