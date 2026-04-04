package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);

    void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
