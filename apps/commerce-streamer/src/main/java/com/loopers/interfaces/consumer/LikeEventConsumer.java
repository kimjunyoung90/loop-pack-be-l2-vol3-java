package com.loopers.interfaces.consumer;

import com.loopers.application.like.LikeEventProcessor;
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
public class LikeEventConsumer {

    private static final String TOPIC_LIKE = "like-events";

    private final LikeEventProcessor likeEventProcessor;

    @KafkaListener(topics = {TOPIC_LIKE}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeLikeEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                likeEventProcessor.process(messages.get(i));
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
