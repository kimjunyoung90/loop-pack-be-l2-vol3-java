package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.CouponOutboxEventRepository;
import com.loopers.domain.outbox.LikeOutboxEventRepository;
import com.loopers.domain.outbox.OrderOutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox 이벤트를 도메인별 테이블에서 폴링하여 Kafka로 발행한다.
 * 트랜잭션이 커밋되어 영속화된 PENDING 이벤트만 조회되므로 TX 경합 문제는 없다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxRelayScheduler {

    private final LikeOutboxEventRepository likeOutboxEventRepository;
    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final CouponOutboxEventRepository couponOutboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Scheduled(fixedDelay = 1000)
    public void relay() {
        likeOutboxEventRepository.findAllByStatus(OutboxStatus.PENDING)
                .forEach(outboxEventPublisher::publish);
        orderOutboxEventRepository.findAllByStatus(OutboxStatus.PENDING)
                .forEach(outboxEventPublisher::publish);
        couponOutboxEventRepository.findAllByStatus(OutboxStatus.PENDING)
                .forEach(outboxEventPublisher::publish);
    }
}
