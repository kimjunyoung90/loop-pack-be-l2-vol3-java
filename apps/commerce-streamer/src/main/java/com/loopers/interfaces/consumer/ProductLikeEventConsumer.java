package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductMetricsService;
import com.loopers.confg.kafka.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductLikeEventConsumer {

    private static final String TOPIC_LIKED = "product-like.LIKED";
    private static final String TOPIC_UNLIKED = "product-like.UNLIKED";

    private final ProductMetricsService productMetricsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {TOPIC_LIKED, TOPIC_UNLIKED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeLikeEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (ConsumerRecord<Object, Object> record : messages) {
            try {
                Long productId = objectMapper.readTree(record.value().toString()).get("productId").asLong();

                if (record.topic().equals(TOPIC_LIKED)) {
                    productMetricsService.incrementLikeCount(productId);
                } else {
                    productMetricsService.decrementLikeCount(productId);
                }
            } catch (Exception e) {
                log.error("좋아요 이벤트 처리 실패. record={}", record, e);
            }
        }
        acknowledgment.acknowledge();
    }
}
