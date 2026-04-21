package com.loopers.domain.outbox;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository<T extends BaseOutboxEvent> {

    T save(T outboxEvent);

    Optional<T> findById(Long id);

    List<T> findAllByStatus(OutboxStatus status);

    void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
