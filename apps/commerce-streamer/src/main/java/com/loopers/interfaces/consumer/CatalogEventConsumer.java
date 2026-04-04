package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductLikeEventProcessor;
import com.loopers.application.product.ProductMetricsEventProcessor;
import com.loopers.confg.kafka.KafkaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CatalogEventConsumer {

    private static final String TOPIC_CATALOG = "catalog-events";

    private final ProductMetricsEventProcessor metricsProcessor;
    private final ProductLikeEventProcessor likeProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {TOPIC_CATALOG}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeCatalogEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                processRecord(messages.get(i));
            } catch (DataIntegrityViolationException e) {
                log.info("카탈로그 이벤트 중복 처리 감지 (정상). record={}", messages.get(i));
            } catch (Exception e) {
                log.error("카탈로그 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("카탈로그 이벤트 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }

    private void processRecord(ConsumerRecord<Object, Object> record) throws Exception {
        JsonNode node = objectMapper.readTree(record.value().toString());
        String eventType = node.get("eventType").asText();

        switch (eventType) {
            case "PRODUCT_VIEWED" -> metricsProcessor.processViewEvent(record);
            case "PRODUCT_LIKED", "PRODUCT_UNLIKED" -> likeProcessor.processRecord(record);
            default -> log.warn("알 수 없는 eventType: {}", eventType);
        }
    }
}
