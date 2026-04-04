package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductMetricsService;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductMetricsEventConsumer {

    private static final String TOPIC_VIEWED = "product-metrics.VIEWED";
    private static final String TOPIC_ORDER_PLACED = "order-metrics.PLACED";

    private final ProductMetricsService productMetricsService;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;

    // TODO(human): ProductLikeEventConsumer를 참고하여 두 메서드에 멱등 처리를 추가하세요.
    //
    // 요구사항:
    // 1. record에서 eventId 헤더를 추출 (extractEventId 메서드 활용)
    // 2. eventId가 이미 처리되었으면 skip (eventHandledRepository.existsByEventId)
    // 3. 비즈니스 로직 처리 후 eventId를 event_handled 테이블에 저장

	@KafkaListener(topics = {TOPIC_VIEWED}, containerFactory = KafkaConfig.BATCH_LISTENER)
	public void consumeProductViewEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
		for (ConsumerRecord<Object, Object> record : messages) {
			try {
				Long productId = objectMapper.readTree(record.value().toString()).get("productId").asLong();

				productMetricsService.incrementViewCount(productId);

			} catch (Exception e) {
				log.error("조회 이벤트 처리 실패. record={}", record, e);
			}
		}
		acknowledgment.acknowledge();
	}

	@KafkaListener(topics = {TOPIC_ORDER_PLACED}, containerFactory = KafkaConfig.BATCH_LISTENER)
	public void consumeOrderPlaceEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
		for (ConsumerRecord<Object, Object> record : messages) {
			try {
				var node = objectMapper.readTree(record.value().toString());
				Long productId = node.get("productId").asLong();
				int quantity = node.get("quantity").asInt();

				productMetricsService.incrementSalesCount(productId, quantity);

			} catch (Exception e) {
				log.error("판매량 이벤트 처리 실패. record={}", record, e);
			}
		}
		acknowledgment.acknowledge();
	}

    private String extractEventId(ConsumerRecord<Object, Object> record) {
        Header header = record.headers().lastHeader("eventId");
        if (header == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
