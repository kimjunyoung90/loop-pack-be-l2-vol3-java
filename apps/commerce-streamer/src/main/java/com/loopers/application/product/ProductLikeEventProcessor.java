package com.loopers.application.product;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class ProductLikeEventProcessor {

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processRecord(ConsumerRecord<Object, Object> record) throws Exception {
        String eventId = extractEventId(record);
        if (eventId != null && eventHandledRepository.existsByEventId(eventId)) {
            return;
        }

        JsonNode node = objectMapper.readTree(record.value().toString());
        Long productId = node.get("productId").asLong();
        String eventType = node.get("eventType").asText();

        if ("PRODUCT_LIKED".equals(eventType)) {
            productMetricsService.incrementLikeCount(productId);
        } else {
            productMetricsService.decrementLikeCount(productId);
        }

        if (eventId != null) {
            eventHandledRepository.save(EventHandled.builder().eventId(eventId).build());
        }
    }

    private String extractEventId(ConsumerRecord<Object, Object> record) {
        Header header = record.headers().lastHeader("eventId");
        if (header == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
