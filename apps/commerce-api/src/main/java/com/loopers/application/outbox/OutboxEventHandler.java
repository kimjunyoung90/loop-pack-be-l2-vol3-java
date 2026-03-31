package com.loopers.application.outbox;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class OutboxEventHandler {

    private static final String AGGREGATE_TYPE_PRODUCT_LIKE = "ProductLike";
    private static final String EVENT_TYPE_LIKED = "LIKED";
    private static final String EVENT_TYPE_UNLIKED = "UNLIKED";

    private final OutboxEventRepository outboxEventRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(AGGREGATE_TYPE_PRODUCT_LIKE)
                .aggregateId(event.productId())
                .eventType(EVENT_TYPE_LIKED)
                .payload("{\"productId\":" + event.productId() + "}")
                .build();
        outboxEventRepository.save(outboxEvent);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(AGGREGATE_TYPE_PRODUCT_LIKE)
                .aggregateId(event.productId())
                .eventType(EVENT_TYPE_UNLIKED)
                .payload("{\"productId\":" + event.productId() + "}")
                .build();
        outboxEventRepository.save(outboxEvent);
    }
}
