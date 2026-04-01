package com.loopers.application.product;

import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsEventHandler {

    private static final String TOPIC_PRODUCT_VIEWED = "product-metrics.VIEWED";
    private static final String TOPIC_PRODUCT_SOLD = "product-metrics.SOLD";

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    // TODO(human): 조회 이벤트와 판매 이벤트를 Kafka로 전송하는 핸들러 2개를 구현하세요.
    //
    // 1. handleProductViewed(ProductViewedEvent)
    //    - 토픽: TOPIC_PRODUCT_VIEWED
    //    - key: productId
    //    - payload: {"productId": 123}
    //
    // 2. handleOrderPlaced(OrderPlacedEvent)
    //    - 주문에 포함된 상품별로 각각 Kafka 전송
    //    - 토픽: TOPIC_PRODUCT_SOLD
    //    - key: productId
    //    - payload: {"productId": 123, "quantity": 2}
    //
    // 힌트:
    // - @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // - kafkaTemplate.send(topic, key, payload) — 비동기 전송 (결과 대기 불필요)
    // - OrderPlacedEvent.stockItems()로 상품 목록 순회
}
