package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxStatus;
import com.loopers.domain.outbox.ProductOutboxEvent;
import com.loopers.domain.outbox.ProductOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductOutboxEventRepositoryImpl implements ProductOutboxEventRepository {

    private final ProductOutboxEventJpaRepository jpaRepository;

    @Override
    public ProductOutboxEvent save(ProductOutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public Optional<ProductOutboxEvent> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ProductOutboxEvent> findAllByStatus(OutboxStatus status) {
        return jpaRepository.findAllByStatus(status);
    }

    @Override
    public void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before) {
        jpaRepository.deleteAllByStatusAndCreatedAtBefore(status, before);
    }
}
