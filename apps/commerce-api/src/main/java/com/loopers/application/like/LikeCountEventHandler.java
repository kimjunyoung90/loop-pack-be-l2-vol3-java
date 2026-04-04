package com.loopers.application.like;

import com.loopers.application.product.ProductService;
import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class LikeCountEventHandler {

    private static final String TOPIC_LIKED = "product-like.LIKED";
    private static final String TOPIC_UNLIKED = "product-like.UNLIKED";

    private final ProductService productService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        productService.incrementLikeCount(event.productId());
        kafkaTemplate.send(TOPIC_LIKED, String.valueOf(event.productId()), "{\"productId\":" + event.productId() + "}");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        productService.decrementLikeCount(event.productId());
        kafkaTemplate.send(TOPIC_UNLIKED, String.valueOf(event.productId()), "{\"productId\":" + event.productId() + "}");
    }
}
