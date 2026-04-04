package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxPublishEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxImmediatePublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Async("outboxTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(OutboxPublishEvent event) {
        outboxEventRepository.findById(event.outboxEventId()).ifPresent(this::attemptPublish);
    }

    private void attemptPublish(OutboxEvent outboxEvent) {
        if (!outboxEvent.isPending()) {
            return;
        }

        try {
            ProducerRecord<Object, Object> record = new ProducerRecord<>(
                    outboxEvent.getTopic(),
                    outboxEvent.getMessageKey(),
                    outboxEvent.getPayload()
            );
            record.headers().add("eventId", outboxEvent.getEventId().getBytes());
            kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);
            outboxEvent.markPublished();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Outbox 즉시 발행 인터럽트. id={}, 스케줄러가 보상 처리 예정", outboxEvent.getId());
        } catch (ExecutionException | TimeoutException e) {
            log.warn("Outbox 즉시 발행 실패. id={}, 스케줄러가 보상 처리 예정", outboxEvent.getId());
        }
    }
}
