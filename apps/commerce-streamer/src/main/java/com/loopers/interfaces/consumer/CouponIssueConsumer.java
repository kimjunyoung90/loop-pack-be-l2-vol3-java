package com.loopers.interfaces.consumer;

import com.loopers.application.coupon.CouponIssueProcessor;
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
public class CouponIssueConsumer {

    private static final String TOPIC_COUPON_ISSUE = "coupon-issue-requests";

    private final CouponIssueProcessor couponIssueProcessor;

    @KafkaListener(topics = {TOPIC_COUPON_ISSUE}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeCouponIssueEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (int i = 0; i < messages.size(); i++) {
            try {
                couponIssueProcessor.process(messages.get(i));
            } catch (DataIntegrityViolationException e) {
                log.info("쿠폰 발급 이벤트 중복 처리 감지 (정상). record={}", messages.get(i));
            } catch (Exception e) {
                log.error("쿠폰 발급 이벤트 처리 실패. record={}", messages.get(i), e);
                throw new BatchListenerFailedException("쿠폰 발급 이벤트 처리 실패", e, i);
            }
        }
        acknowledgment.acknowledge();
    }
}
