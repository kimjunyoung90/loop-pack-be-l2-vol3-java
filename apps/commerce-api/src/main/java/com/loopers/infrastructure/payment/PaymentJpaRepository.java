package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndDeletedAtIsNull(Long id);

    Optional<Payment> findByTransactionKeyAndDeletedAtIsNull(String transactionKey);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.deletedAt IS NULL
              AND (
                    p.status = com.loopers.domain.payment.PaymentStatus.UNKNOWN
                 OR (p.status = com.loopers.domain.payment.PaymentStatus.PENDING
                     AND p.updatedAt < :pendingThreshold)
              )
            """)
    List<Payment> findRecoveryTargets(@Param("pendingThreshold") ZonedDateTime pendingThreshold,
                                      Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.deletedAt IS NULL
              AND p.status IN (
                    com.loopers.domain.payment.PaymentStatus.PENDING,
                    com.loopers.domain.payment.PaymentStatus.UNKNOWN
              )
              AND p.createdAt < :giveUpThreshold
            """)
    List<Payment> findGiveUpTargets(@Param("giveUpThreshold") ZonedDateTime giveUpThreshold,
                                    Pageable pageable);
}
