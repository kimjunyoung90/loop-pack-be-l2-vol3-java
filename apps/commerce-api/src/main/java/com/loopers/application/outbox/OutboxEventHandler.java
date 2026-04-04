package com.loopers.application.outbox;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        outboxEventRepository.save(OutboxEvent.builder()
                .topic(TOPIC_LIKED)
                .messageKey(String.valueOf(event.productId()))
                .payload("{\"productId\":" + event.productId() + "}")
                .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        outboxEventRepository.save(OutboxEvent.builder()
                .topic(TOPIC_UNLIKED)
                .messageKey(String.valueOf(event.productId()))
                .payload("{\"productId\":" + event.productId() + "}")
                .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductViewed(ProductViewedEvent event) {
        outboxEventRepository.save(OutboxEvent.builder()
                .topic(TOPIC_VIEWED)
                .messageKey(String.valueOf(event.productId()))
                .payload("{\"productId\":" + event.productId() + "}")
                .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        event.stockItems().forEach(stockItem ->
                outboxEventRepository.save(OutboxEvent.builder()
                        .topic(TOPIC_ORDER_PLACED)
                        .messageKey(String.valueOf(stockItem.productId()))
                        .payload("{\"productId\":" + stockItem.productId() + ",\"quantity\":" + stockItem.quantity() + "}")
                        .build()));
    }
}
