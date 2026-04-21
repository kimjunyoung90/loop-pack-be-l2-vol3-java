package com.loopers.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.outbox.LikeOutboxEventService;
import com.loopers.application.outbox.OrderOutboxEventService;
import com.loopers.application.outbox.ProductOutboxEventService;
import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * 통계 이벤트를 Outbox Pattern으로 발행한다.
 * 도메인별 Outbox 테이블에 기록하도록 도메인별 Service로 분기한다.
 * BEFORE_COMMIT 단계에서 동일 트랜잭션에 Outbox 레코드를 저장해 메시지 유실을 방지한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsEventPublisher {

    private static final String TOPIC_PRODUCT = "product-events";
    private static final String TOPIC_LIKE = "like-events";
    private static final String TOPIC_ORDER = "order-events";

    private static final String EVENT_PRODUCT_VIEWED = "PRODUCT_VIEWED";
    private static final String EVENT_PRODUCT_LIKED = "PRODUCT_LIKED";
    private static final String EVENT_PRODUCT_UNLIKED = "PRODUCT_UNLIKED";
    private static final String EVENT_ORDER_PLACED = "ORDER_PLACED";

    private final ProductOutboxEventService productOutboxEventService;
    private final LikeOutboxEventService likeOutboxEventService;
    private final OrderOutboxEventService orderOutboxEventService;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        likeOutboxEventService.saveAndPublish(
                TOPIC_LIKE,
                String.valueOf(event.productId()),
                writeJson(Map.of("eventType", EVENT_PRODUCT_LIKED, "productId", event.productId()))
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        likeOutboxEventService.saveAndPublish(
                TOPIC_LIKE,
                String.valueOf(event.productId()),
                writeJson(Map.of("eventType", EVENT_PRODUCT_UNLIKED, "productId", event.productId()))
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductViewed(ProductViewedEvent event) {
        productOutboxEventService.saveAndPublish(
                TOPIC_PRODUCT,
                String.valueOf(event.productId()),
                writeJson(Map.of("eventType", EVENT_PRODUCT_VIEWED, "productId", event.productId()))
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        event.stockItems().forEach(stockItem -> orderOutboxEventService.saveAndPublish(
                TOPIC_ORDER,
                String.valueOf(stockItem.productId()),
                writeJson(Map.of(
                        "eventType", EVENT_ORDER_PLACED,
                        "productId", stockItem.productId(),
                        "quantity", stockItem.quantity()
                ))
        ));
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
