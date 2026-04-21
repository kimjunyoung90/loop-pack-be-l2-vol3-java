package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.LikeOutboxEvent;
import com.loopers.domain.outbox.LikeOutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class LikeOutboxEventRepositoryImpl implements LikeOutboxEventRepository {

    private final LikeOutboxEventJpaRepository jpaRepository;

    @Override
    public LikeOutboxEvent save(LikeOutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public Optional<LikeOutboxEvent> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<LikeOutboxEvent> findAllByStatus(OutboxStatus status) {
        return jpaRepository.findAllByStatus(status);
    }

    @Override
    public void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before) {
        jpaRepository.deleteAllByStatusAndCreatedAtBefore(status, before);
    }
}
