package com.loopers.infrastructure.kafka;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

/**
 * 통계 이벤트 fire-and-forget 발행.
 * 유실돼도 비즈니스 불변식이 깨지지 않는 이벤트는 Outbox 없이 직접 발행한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsEventPublisher {

    private static final String TOPIC_LIKED = "product-like.LIKED";
    private static final String TOPIC_UNLIKED = "product-like.UNLIKED";
    private static final String TOPIC_VIEWED = "product-metrics.VIEWED";
    private static final String TOPIC_ORDER_PLACED = "order-metrics.PLACED";

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("outboxTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        send(TOPIC_LIKED, String.valueOf(event.productId()),
                writeJson(Map.of("productId", event.productId())));
    }

    @Async("outboxTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        send(TOPIC_UNLIKED, String.valueOf(event.productId()),
                writeJson(Map.of("productId", event.productId())));
    }

    @Async("outboxTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductViewed(ProductViewedEvent event) {
        send(TOPIC_VIEWED, String.valueOf(event.productId()),
                writeJson(Map.of("productId", event.productId())));
    }

    @Async("outboxTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        event.stockItems().forEach(stockItem ->
                send(TOPIC_ORDER_PLACED, String.valueOf(stockItem.productId()),
                        writeJson(Map.of("productId", stockItem.productId(), "quantity", stockItem.quantity()))));
    }

    private void send(String topic, String key, String payload) {
        try {
            ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, payload);
            record.headers().add("eventId", UUID.randomUUID().toString().getBytes());
            kafkaTemplate.send(record);
        } catch (Exception e) {
            log.warn("통계 이벤트 발행 실패 (유실 허용). topic={}, key={}", topic, key, e);
        }
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
