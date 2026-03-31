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

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxEventPublisher {

    private static final String TOPIC_PREFIX = "product-like.";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    // TODO(human): Outbox 폴링 → Kafka 발행 → 상태 변경 로직을 구현하세요.
    //
    // 요구사항:
    // - PENDING 상태의 OutboxEvent를 조회
    // - 각 이벤트를 Kafka 토픽으로 전송 (토픽명: TOPIC_PREFIX + eventType, key: aggregateId)
    // - 전송 성공 시 markPublished() 호출
    // - 실패 시 로그를 남기고 다음 이벤트 계속 처리
    //
    // 힌트:
    // - @Scheduled(fixedDelay = 3000) 으로 3초마다 폴링
    // - kafkaTemplate.send(topic, key, payload) 사용
    // - 전송 결과는 CompletableFuture로 반환됨
}
