package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Outbox 보상 스케줄러.
 * 즉시 발행(OutboxImmediatePublisher)이 실패한 이벤트를 보상 처리한다.
 * GRACE_PERIOD_SECONDS 이내의 이벤트는 즉시 발행 경로와의 경합을 피하기 위해 건너뛴다.
 */
@Slf4j
@Component
public class OutboxEventPublisher {

    private static final int GRACE_PERIOD_SECONDS = 10;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final OutboxEventPublisher self;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository,
                                KafkaTemplate<Object, Object> kafkaTemplate,
                                @Lazy OutboxEventPublisher self) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.self = self;
    }

    @Scheduled(fixedDelay = 10000)
    public void publish() {
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(GRACE_PERIOD_SECONDS);
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllByStatusAndCreatedAtBefore(
                OutboxStatus.PENDING, before);
        outboxEvents.forEach(self::publishEvent);
    }

    @Transactional
    public void publishEvent(OutboxEvent outboxEvent) {
        try {
            ProducerRecord<Object, Object> record = new ProducerRecord<>(
                    outboxEvent.getTopic(),
                    outboxEvent.getMessageKey(),
                    outboxEvent.getPayload()
            );
            record.headers().add("eventId", outboxEvent.getEventId().getBytes());
            kafkaTemplate.send(record).get();
            outboxEvent.markPublished();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleRetry(outboxEvent);
            log.error("Outbox 이벤트 발행 중 인터럽트 발생. id={}, retryCount={}", outboxEvent.getId(), outboxEvent.getRetryCount(), e);
        } catch (ExecutionException e) {
            handleRetry(outboxEvent);
            log.error("Outbox 이벤트 발행 실패. id={}, retryCount={}", outboxEvent.getId(), outboxEvent.getRetryCount(), e);
        }
    }

    private void handleRetry(OutboxEvent outboxEvent) {
        outboxEvent.recordFailure();
        if (outboxEvent.isFailed() && !outboxEvent.isDeadLetter()) {
            outboxEventRepository.save(outboxEvent.createDeadLetterEvent());
            log.warn("Outbox 이벤트 최대 재시도 초과. DLQ 이벤트 생성. id={}", outboxEvent.getId());
        }
    }
}
