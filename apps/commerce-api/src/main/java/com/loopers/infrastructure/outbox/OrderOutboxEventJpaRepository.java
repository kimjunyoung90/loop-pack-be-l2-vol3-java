package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OrderOutboxEvent;
import com.loopers.domain.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface OrderOutboxEventJpaRepository extends JpaRepository<OrderOutboxEvent, Long> {

    List<OrderOutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);

    void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
