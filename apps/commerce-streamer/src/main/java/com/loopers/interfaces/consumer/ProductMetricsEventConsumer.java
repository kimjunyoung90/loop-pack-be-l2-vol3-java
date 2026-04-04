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
public class ProductMetricsEventConsumer {

    private static final String TOPIC_VIEWED = "product-metrics.VIEWED";
    private static final String TOPIC_ORDER_PLACED = "order-metrics.PLACED";

    private final ProductMetricsService productMetricsService;
    private final ObjectMapper objectMapper;


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
}
