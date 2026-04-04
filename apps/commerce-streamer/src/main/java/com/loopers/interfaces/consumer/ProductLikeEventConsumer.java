package com.loopers.interfaces.consumer;

import com.loopers.application.product.ProductLikeEventProcessor;
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
public class ProductLikeEventConsumer {

    private static final String TOPIC_LIKED = "product-like.LIKED";
    private static final String TOPIC_UNLIKED = "product-like.UNLIKED";

    private final ProductLikeEventProcessor processor;

    @KafkaListener(topics = {TOPIC_LIKED, TOPIC_UNLIKED}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeLikeEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                processor.processRecord(messages.get(i));
            } catch (DataIntegrityViolationException e) {
                log.info("좋아요 이벤트 중복 처리 감지 (정상). record={}", messages.get(i));
            } catch (Exception e) {
                log.error("좋아요 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("좋아요 이벤트 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }
}
