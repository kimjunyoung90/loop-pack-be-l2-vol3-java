package com.loopers.application.outbox;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxPublishEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class OutboxEventHandler {

    private static final String TOPIC_LIKED = "product-like.LIKED";
    private static final String TOPIC_UNLIKED = "product-like.UNLIKED";
    private static final String TOPIC_VIEWED = "product-metrics.VIEWED";
    private static final String TOPIC_ORDER_PLACED = "order-metrics.PLACED";

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        OutboxEvent outboxEvent = saveOutboxEvent(TOPIC_LIKED,
                String.valueOf(event.productId()),
                "{\"productId\":" + event.productId() + "}");
        eventPublisher.publishEvent(new OutboxPublishEvent(outboxEvent.getId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        OutboxEvent outboxEvent = saveOutboxEvent(TOPIC_UNLIKED,
                String.valueOf(event.productId()),
                "{\"productId\":" + event.productId() + "}");
        eventPublisher.publishEvent(new OutboxPublishEvent(outboxEvent.getId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductViewed(ProductViewedEvent event) {
        OutboxEvent outboxEvent = saveOutboxEvent(TOPIC_VIEWED,
                String.valueOf(event.productId()),
                "{\"productId\":" + event.productId() + "}");
        eventPublisher.publishEvent(new OutboxPublishEvent(outboxEvent.getId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        event.stockItems().forEach(stockItem -> {
            OutboxEvent outboxEvent = saveOutboxEvent(TOPIC_ORDER_PLACED,
                    String.valueOf(stockItem.productId()),
                    "{\"productId\":" + stockItem.productId() + ",\"quantity\":" + stockItem.quantity() + "}");
            eventPublisher.publishEvent(new OutboxPublishEvent(outboxEvent.getId()));
        });
    }

    private OutboxEvent saveOutboxEvent(String topic, String messageKey, String payload) {
        return outboxEventRepository.save(OutboxEvent.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .build());
    }
}
