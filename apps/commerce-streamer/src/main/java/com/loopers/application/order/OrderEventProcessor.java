package com.loopers.application.order;

import com.loopers.application.product.ProductMetricsService;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
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
public class OrderEventProcessor {

    private static final String EVENT_ORDER_PLACED = "ORDER_PLACED";
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
        var node = objectMapper.readTree(record.value().toString());
        String eventType = node.get("eventType").asText();

        if (EVENT_ORDER_PLACED.equals(eventType)) {
            Long productId = node.get("productId").asLong();
            int quantity = node.get("quantity").asInt();
            productMetricsService.incrementSalesCount(productId, quantity, eventDate);
        } else {
            log.warn("알 수 없는 주문 이벤트 타입: {}", eventType);
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
