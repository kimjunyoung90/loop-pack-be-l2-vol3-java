package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OrderOutboxEvent;
import com.loopers.domain.outbox.OrderOutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderOutboxEventRepositoryImpl implements OrderOutboxEventRepository {

    private final OrderOutboxEventJpaRepository jpaRepository;

    @Override
    public OrderOutboxEvent save(OrderOutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public Optional<OrderOutboxEvent> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<OrderOutboxEvent> findAllByStatus(OutboxStatus status) {
        return jpaRepository.findAllByStatus(status);
    }

    @Override
    public void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before) {
        jpaRepository.deleteAllByStatusAndCreatedAtBefore(status, before);
    }
}
