package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductEventProcessor;
import com.loopers.application.product.ProductRankingService;
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
public class ProductEventConsumer {

    private static final String TOPIC_PRODUCT = "product-events";

    private final ProductEventProcessor productEventProcessor;
    private final ProductRankingService productRankingService;

    @KafkaListener(topics = {TOPIC_PRODUCT}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeProductEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                productEventProcessor.process(messages.get(i));
            } catch (DataIntegrityViolationException e) {
                log.info("상품 이벤트 중복 처리 감지 (정상). record={}", messages.get(i));
            } catch (Exception e) {
                log.error("상품 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("상품 이벤트 처리 실패", e, i);
            }
        }
        productRankingService.flush();
        acknowledgment.acknowledge();
    }
}
