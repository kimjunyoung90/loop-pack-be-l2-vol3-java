package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return outboxEventJpaRepository.save(outboxEvent);
    }

    @Override
    public Optional<OutboxEvent> findById(Long id) {
        return outboxEventJpaRepository.findById(id);
    }

    @Override
    public List<OutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before) {
        return outboxEventJpaRepository.findAllByStatusAndCreatedAtBefore(status, before);
    }
}
