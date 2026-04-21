package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.CouponOutboxEvent;
import com.loopers.domain.outbox.CouponOutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CouponOutboxEventRepositoryImpl implements CouponOutboxEventRepository {

    private final CouponOutboxEventJpaRepository jpaRepository;

    @Override
    public CouponOutboxEvent save(CouponOutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public Optional<CouponOutboxEvent> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CouponOutboxEvent> findAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before) {
        return jpaRepository.findAllByStatusAndCreatedAtBefore(status, before);
    }

    @Override
    public void deleteAllByStatusAndCreatedAtBefore(OutboxStatus status, ZonedDateTime before) {
        jpaRepository.deleteAllByStatusAndCreatedAtBefore(status, before);
    }
}
