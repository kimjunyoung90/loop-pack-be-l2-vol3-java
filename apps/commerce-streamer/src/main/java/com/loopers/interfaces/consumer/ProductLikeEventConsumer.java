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
public class ProductLikeEventConsumer {

    private static final String TOPIC_LIKED = "product-like.LIKED";
    private static final String TOPIC_UNLIKED = "product-like.UNLIKED";
    private static final String DLT_SUFFIX = ".DLT";

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ProductLikeEventConsumer self;

    public ProductLikeEventConsumer(ProductMetricsService productMetricsService,
                                    EventHandledRepository eventHandledRepository,
                                    ObjectMapper objectMapper,
                                    KafkaTemplate<Object, Object> kafkaTemplate,
                                    ProductLikeEventConsumer self) {
        this.productMetricsService = productMetricsService;
        this.eventHandledRepository = eventHandledRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.self = self;
    }

    @KafkaListener(topics = {TOPIC_LIKED, TOPIC_UNLIKED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeLikeEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (ConsumerRecord<Object, Object> record : messages) {
            try {
                self.processRecord(record);
            } catch (Exception e) {
                log.error("좋아요 이벤트 처리 실패. DLQ로 전송. record={}", record, e);
                sendToDlq(record);
            }
        }
        acknowledgment.acknowledge();
    }

    @Transactional
    public void processRecord(ConsumerRecord<Object, Object> record) throws Exception {
        String eventId = extractEventId(record);
        if (eventId != null && eventHandledRepository.existsByEventId(eventId)) {
            return;
        }

        Long productId = objectMapper.readTree(record.value().toString()).get("productId").asLong();

        if (record.topic().equals(TOPIC_LIKED)) {
            productMetricsService.incrementLikeCount(productId);
        } else {
            productMetricsService.decrementLikeCount(productId);
        }

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
