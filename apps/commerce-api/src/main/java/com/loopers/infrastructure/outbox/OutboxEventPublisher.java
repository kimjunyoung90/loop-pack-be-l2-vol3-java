package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventPublisher {

    private static final String TOPIC_PREFIX = "product-like.";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publish() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAllByStatus(OutboxStatus.PENDING);
        outboxEvents.forEach(outboxEvent -> {
            try {
                kafkaTemplate.send(
                        TOPIC_PREFIX + outboxEvent.getEventType(),
                        String.valueOf(outboxEvent.getAggregateId()),
                        outboxEvent.getPayload()
                ).get();
                outboxEvent.markPublished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Outbox 이벤트 발행 중 인터럽트 발생. id={}", outboxEvent.getId(), e);
            } catch (ExecutionException e) {
                log.error("Outbox 이벤트 발행 실패. id={}", outboxEvent.getId(), e);
            }
        });
    }
}
