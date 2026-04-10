package com.loopers.application.like;

import com.loopers.application.product.ProductMetricsService;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class LikeEventProcessor {

    private static final String EVENT_PRODUCT_LIKED = "PRODUCT_LIKED";
    private static final String EVENT_PRODUCT_UNLIKED = "PRODUCT_UNLIKED";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(ConsumerRecord<Object, Object> record) throws Exception {
        String eventId = extractEventId(record);
        if (eventId != null && eventHandledRepository.existsByEventId(eventId)) {
            return;
        }

        LocalDate eventDate = LocalDate.ofInstant(Instant.ofEpochMilli(record.timestamp()), KST);
        JsonNode node = objectMapper.readTree(record.value().toString());
        Long productId = node.get("productId").asLong();
        String eventType = node.get("eventType").asText();

        if (EVENT_PRODUCT_LIKED.equals(eventType)) {
            productMetricsService.incrementLikeCount(productId, eventDate);
        } else if (EVENT_PRODUCT_UNLIKED.equals(eventType)) {
            productMetricsService.decrementLikeCount(productId, eventDate);
        } else {
            log.warn("알 수 없는 좋아요 이벤트 타입: {}", eventType);
            return;
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
