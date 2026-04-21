package com.loopers.application.like;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.like.result.LikeResult;
import com.loopers.application.outbox.LikeOutboxEventService;
import com.loopers.application.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private static final String TOPIC_LIKE = "like-events";
    private static final String EVENT_PRODUCT_LIKED = "PRODUCT_LIKED";
    private static final String EVENT_PRODUCT_UNLIKED = "PRODUCT_UNLIKED";

    private final LikeService likeService;
    private final ProductService productService;
    private final LikeOutboxEventService likeOutboxEventService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LikeResult like(Long userId, Long productId) {
        LikeResult result = likeService.like(userId, productId);
        productService.incrementLikeCount(productId);
		//outbox 이벤트 적재
        likeOutboxEventService.saveAndPublish(
                TOPIC_LIKE,
                String.valueOf(productId),
                writeJson(Map.of("eventType", EVENT_PRODUCT_LIKED, "productId", productId))
        );
        return result;
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        likeService.unlike(userId, productId);
        productService.decrementLikeCount(productId);
		//outbox 이벤트 적재
        likeOutboxEventService.saveAndPublish(
                TOPIC_LIKE,
                String.valueOf(productId),
                writeJson(Map.of("eventType", EVENT_PRODUCT_UNLIKED, "productId", productId))
        );
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
