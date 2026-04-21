package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.CouponOutboxEvent;
import com.loopers.domain.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface CouponOutboxEventJpaRepository extends JpaRepository<CouponOutboxEvent, Long> {

    List<CouponOutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);

    void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
