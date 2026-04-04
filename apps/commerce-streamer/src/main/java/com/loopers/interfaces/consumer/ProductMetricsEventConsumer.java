package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductMetricsService;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class ProductMetricsEventConsumer {

    private static final String TOPIC_VIEWED = "product-metrics.VIEWED";
    private static final String TOPIC_ORDER_PLACED = "order-metrics.PLACED";

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;
    private final ProductMetricsEventConsumer self;

    public ProductMetricsEventConsumer(ProductMetricsService productMetricsService,
                                       EventHandledRepository eventHandledRepository,
                                       ObjectMapper objectMapper,
                                       @Lazy ProductMetricsEventConsumer self) {
        this.productMetricsService = productMetricsService;
        this.eventHandledRepository = eventHandledRepository;
        this.objectMapper = objectMapper;
        this.self = self;
    }

    @KafkaListener(topics = {TOPIC_VIEWED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeProductViewEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                self.processViewEvent(messages.get(i));
            } catch (Exception e) {
                log.error("조회 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("조회 이벤트 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = {TOPIC_ORDER_PLACED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeOrderPlaceEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                self.processOrderEvent(messages.get(i));
            } catch (Exception e) {
                log.error("판매량 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("판매량 이벤트 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }

    @Transactional
    public void processViewEvent(ConsumerRecord<Object, Object> record) throws Exception {
        String eventId = extractEventId(record);
        if (eventId != null && eventHandledRepository.existsByEventId(eventId)) {
            return;
        }

        Long productId = objectMapper.readTree(record.value().toString()).get("productId").asLong();
        productMetricsService.incrementViewCount(productId);

        if (eventId != null) {
            eventHandledRepository.save(EventHandled.builder().eventId(eventId).build());
        }
    }

    @Transactional
    public void processOrderEvent(ConsumerRecord<Object, Object> record) throws Exception {
        String eventId = extractEventId(record);
        if (eventId != null && eventHandledRepository.existsByEventId(eventId)) {
            return;
        }

        var node = objectMapper.readTree(record.value().toString());
        Long productId = node.get("productId").asLong();
        int quantity = node.get("quantity").asInt();
        productMetricsService.incrementSalesCount(productId, quantity);

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
