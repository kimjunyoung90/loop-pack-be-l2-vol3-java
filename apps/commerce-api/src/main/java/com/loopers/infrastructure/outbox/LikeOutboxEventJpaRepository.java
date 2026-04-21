package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.LikeOutboxEvent;
import com.loopers.domain.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface LikeOutboxEventJpaRepository extends JpaRepository<LikeOutboxEvent, Long> {

    List<LikeOutboxEvent> findAllByStatus(OutboxStatus status);

    void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before);
}
