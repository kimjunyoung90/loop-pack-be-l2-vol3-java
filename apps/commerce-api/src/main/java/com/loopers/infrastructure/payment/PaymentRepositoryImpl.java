package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByIdAndDeletedAtIsNull(Long id) {
        return paymentJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Optional<Payment> findByTransactionKeyAndDeletedAtIsNull(String transactionKey) {
        return paymentJpaRepository.findByTransactionKeyAndDeletedAtIsNull(transactionKey);
    }

    @Override
    public List<Payment> findRecoveryTargets(ZonedDateTime pendingThreshold, Pageable pageable) {
        return paymentJpaRepository.findRecoveryTargets(pendingThreshold, pageable);
    }
}
