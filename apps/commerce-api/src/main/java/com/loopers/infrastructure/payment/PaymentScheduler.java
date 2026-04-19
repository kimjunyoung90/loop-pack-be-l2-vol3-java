package com.loopers.infrastructure.payment;

import com.loopers.application.payment.PaymentRecoveryFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "payment.reconciliation.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentScheduler {

    private final PaymentRecoveryFacade paymentRecoveryFacade;

    @Scheduled(fixedDelayString = "${payment.reconciliation.interval-ms:30000}")
    public void reconcile() {
        try {
            paymentRecoveryFacade.reconcilePendingPayments();
        } catch (Exception e) {
            log.error("Reconciliation 스케줄러 실행 중 예외", e);
        }
    }
}
