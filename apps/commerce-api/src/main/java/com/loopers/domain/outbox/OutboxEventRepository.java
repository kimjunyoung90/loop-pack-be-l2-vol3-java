package com.loopers.domain.outbox;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

    Optional<OutboxEvent> findById(Long id);

    List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
