package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentGatewayClient;
import com.loopers.domain.payment.PaymentGatewayClient.ReconciliationResult;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentRecoveryFacade {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final PaymentService paymentService;

    @Value("${payment.reconciliation.pending-grace-seconds:10}")
    private long pendingGraceSeconds;

    @Value("${payment.reconciliation.batch-size:50}")
    private int batchSize;

    @Value("${payment.reconciliation.give-up-minutes:30}")
    private long giveUpMinutes;

    public void reconcilePendingPayments() {
        abandonLongPendingPayments();

        ZonedDateTime pendingThreshold = ZonedDateTime.now().minusSeconds(pendingGraceSeconds);
        List<Payment> targets = paymentRepository.findRecoveryTargets(
                pendingThreshold, PageRequest.of(0, batchSize));

        if (targets.isEmpty()) {
            return;
        }

        log.info("Reconciliation 대상 {} 건 조회", targets.size());

        for (Payment payment : targets) {
            reconcileSafely(payment);
        }
    }

    private void abandonLongPendingPayments() {
        ZonedDateTime giveUpThreshold = ZonedDateTime.now().minusMinutes(giveUpMinutes);
        List<Payment> targets = paymentRepository.findGiveUpTargets(
                giveUpThreshold, PageRequest.of(0, batchSize));

        if (targets.isEmpty()) {
            return;
        }

        log.error("[ALERT] 최종 포기 대상 결제 {} 건 발견 — 생성 후 {}분 이상 미확정",
                targets.size(), giveUpMinutes);

        String reason = String.format("최종 포기 — %d분 이상 미확정", giveUpMinutes);
        for (Payment payment : targets) {
            try {
                log.error("[ALERT] 결제 최종 포기: paymentId={}, orderId={}, status={}, createdAt={}",
                        payment.getId(), payment.getOrderId(), payment.getStatus(), payment.getCreatedAt());
                paymentService.abandonPayment(payment.getId(), reason);
            } catch (Exception e) {
                log.warn("최종 포기 처리 실패 — 다음 주기 재시도: paymentId={}", payment.getId(), e);
            }
        }
    }

    private void reconcileSafely(Payment payment) {
        try {
            Optional<ReconciliationResult> result = paymentGatewayClient
                    .findByOrderId(payment.getUserId(), payment.getOrderId());

            if (result.isEmpty()) {
                paymentService.reconcileAsNotFound(payment.getId());
            } else {
                paymentService.reconcileByResult(payment.getId(), result.get());
            }
        } catch (Exception e) {
            log.warn("Reconciliation 실패 — 다음 주기 재시도: paymentId={}", payment.getId(), e);
        }
    }
}
