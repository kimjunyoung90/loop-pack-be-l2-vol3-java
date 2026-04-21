package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxStatus;
import com.loopers.domain.outbox.ProductOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface ProductOutboxEventJpaRepository extends JpaRepository<ProductOutboxEvent, Long> {

    List<ProductOutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);

    void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
