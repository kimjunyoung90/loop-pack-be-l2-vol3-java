package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Outbox 이벤트를 Kafka로 발행하는 책임만 담당.
 * 즉시 발행(OutboxImmediatePublisher)과 보상 스케줄러(OutboxRelayScheduler)가 공통으로 사용한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Transactional
    public void publish(OutboxEvent outboxEvent) {
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
            handleRetry(outboxEvent);
            log.error("Outbox 이벤트 발행 중 인터럽트 발생. id={}, retryCount={}", outboxEvent.getId(), outboxEvent.getRetryCount(), e);
        } catch (ExecutionException | TimeoutException e) {
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
