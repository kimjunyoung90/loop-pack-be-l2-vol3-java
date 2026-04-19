package com.loopers.domain.payment;

import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdAndDeletedAtIsNull(Long id);

    Optional<Payment> findByTransactionKeyAndDeletedAtIsNull(String transactionKey);

    List<Payment> findRecoveryTargets(ZonedDateTime pendingThreshold, Pageable pageable);
}
