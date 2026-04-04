package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxPublishEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * TX 커밋 직후 Outbox 이벤트를 즉시 발행 시도.
 * 실패 시 OutboxRelayScheduler가 보상 처리한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxImmediatePublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Async("outboxTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(OutboxPublishEvent event) {
        outboxEventRepository.findById(event.outboxEventId())
                .ifPresent(outboxEventPublisher::publish);
    }
}
