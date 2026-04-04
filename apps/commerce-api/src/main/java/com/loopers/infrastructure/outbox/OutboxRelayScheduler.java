package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * Outbox 보상 스케줄러.
 * 즉시 발행(OutboxImmediatePublisher)이 실패한 이벤트를 보상 처리한다.
 * GRACE_PERIOD_SECONDS 이내의 이벤트는 즉시 발행 경로와의 경합을 피하기 위해 건너뛴다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxRelayScheduler {

    private static final int GRACE_PERIOD_SECONDS = 10;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Scheduled(fixedDelay = 10000)
    public void relay() {
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(GRACE_PERIOD_SECONDS);
        outboxEventRepository.findAllByStatusAndCreatedAtBefore(OutboxStatus.PENDING, before)
                .forEach(outboxEventPublisher::publish);
    }
}
