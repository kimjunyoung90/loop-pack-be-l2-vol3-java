package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final OutboxEventPublisher self;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository,
                                KafkaTemplate<Object, Object> kafkaTemplate,
                                OutboxEventPublisher self) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.self = self;
    }

    @Scheduled(fixedDelay = 3000)
    public void publish() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllByStatus(OutboxStatus.PENDING);
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
