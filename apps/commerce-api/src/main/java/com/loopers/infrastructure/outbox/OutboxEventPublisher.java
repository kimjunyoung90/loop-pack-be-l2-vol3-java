package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.BaseOutboxEvent;
import com.loopers.domain.outbox.CouponOutboxEvent;
import com.loopers.domain.outbox.CouponOutboxEventRepository;
import com.loopers.domain.outbox.LikeOutboxEvent;
import com.loopers.domain.outbox.LikeOutboxEventRepository;
import com.loopers.domain.outbox.OrderOutboxEvent;
import com.loopers.domain.outbox.OrderOutboxEventRepository;
import com.loopers.domain.outbox.ProductOutboxEvent;
import com.loopers.domain.outbox.ProductOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 도메인 Outbox 이벤트를 Kafka로 발행한다.
 * 즉시 발행(OutboxImmediatePublisher)과 보상 스케줄러(OutboxRelayScheduler)가 공통으로 사용한다.
 * DLQ 저장은 BaseOutboxEvent.getDomain()을 기준으로 도메인 Repository에 위임한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventPublisher {

    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final LikeOutboxEventRepository likeOutboxEventRepository;
    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final CouponOutboxEventRepository couponOutboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Transactional
    public void publish(BaseOutboxEvent outboxEvent) {
        if (!outboxEvent.isPending()) {
            return;
        }

        try {
            ProducerRecord<Object, Object> record = new ProducerRecord<>(
                    outboxEvent.getTopic(),
                    outboxEvent.getMessageKey(),
                    outboxEvent.getPayload()
            );
            record.headers().add("eventId", outboxEvent.getEventId().getBytes(StandardCharsets.UTF_8));
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

    private void handleRetry(BaseOutboxEvent outboxEvent) {
        outboxEvent.recordFailure();
        if (outboxEvent.isFailed() && !outboxEvent.isDeadLetter()) {
            saveDeadLetter(outboxEvent.createDeadLetterEvent());
            log.warn("Outbox 이벤트 최대 재시도 초과. DLQ 이벤트 생성. id={}", outboxEvent.getId());
        }
    }

    private void saveDeadLetter(BaseOutboxEvent dle) {
        switch (dle.getDomain()) {
            case PRODUCT -> productOutboxEventRepository.save((ProductOutboxEvent) dle);
            case LIKE -> likeOutboxEventRepository.save((LikeOutboxEvent) dle);
            case ORDER -> orderOutboxEventRepository.save((OrderOutboxEvent) dle);
            case COUPON -> couponOutboxEventRepository.save((CouponOutboxEvent) dle);
        }
    }
}
