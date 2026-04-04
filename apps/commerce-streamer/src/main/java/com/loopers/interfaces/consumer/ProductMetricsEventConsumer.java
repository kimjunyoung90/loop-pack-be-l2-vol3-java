package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductMetricsService;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    private static final String DLT_SUFFIX = ".DLT";

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ProductMetricsEventConsumer self;

    public ProductMetricsEventConsumer(ProductMetricsService productMetricsService,
                                       EventHandledRepository eventHandledRepository,
                                       ObjectMapper objectMapper,
                                       KafkaTemplate<Object, Object> kafkaTemplate,
                                       ProductMetricsEventConsumer self) {
        this.productMetricsService = productMetricsService;
        this.eventHandledRepository = eventHandledRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.self = self;
    }

    @KafkaListener(topics = {TOPIC_VIEWED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeProductViewEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (ConsumerRecord<Object, Object> record : messages) {
            try {
                self.processViewEvent(record);
            } catch (Exception e) {
                log.error("조회 이벤트 처리 실패. DLQ로 전송. record={}", record, e);
                sendToDlq(record);
            }
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(topics = {TOPIC_ORDER_PLACED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeOrderPlaceEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (ConsumerRecord<Object, Object> record : messages) {
            try {
                self.processOrderEvent(record);
            } catch (Exception e) {
                log.error("판매량 이벤트 처리 실패. DLQ로 전송. record={}", record, e);
                sendToDlq(record);
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

    private void sendToDlq(ConsumerRecord<Object, Object> record) {
        try {
            kafkaTemplate.send(record.topic() + DLT_SUFFIX, record.key(), record.value());
        } catch (Exception dlqEx) {
            log.error("DLQ 전송 실패. record={}", record, dlqEx);
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
