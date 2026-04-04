package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductMetricsEventProcessor;
import com.loopers.confg.kafka.KafkaConfig;
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
public class OrderEventConsumer {

    private static final String TOPIC_ORDER = "order-events";

    private final ProductMetricsEventProcessor processor;

    @KafkaListener(topics = {TOPIC_ORDER}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeOrderEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                processor.processOrderEvent(messages.get(i));
            } catch (DataIntegrityViolationException e) {
                log.info("주문 이벤트 중복 처리 감지 (정상). record={}", messages.get(i));
            } catch (Exception e) {
                log.error("주문 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("주문 이벤트 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }
}
