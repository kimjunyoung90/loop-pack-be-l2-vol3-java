package com.loopers.interfaces.consumer;

import com.loopers.application.coupon.CouponService;
import com.loopers.confg.kafka.KafkaConfig;
import com.fasterxml.jackson.databind.JsonNode;
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
public class CouponIssueConsumer {

    private static final String COUPON_ISSUE_TOPIC = "coupon-issue.request";

    private final CouponService couponService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {COUPON_ISSUE_TOPIC}, containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consumeCouponEvent(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        for (ConsumerRecord<Object, Object> record : messages) {
            try {
                JsonNode node = objectMapper.readTree(record.value().toString());
                Long userId = node.get("userId").asLong();
                Long couponId = node.get("couponId").asLong();
                Long issueRequestId = node.get("issueRequestId").asLong();
                couponService.issueCoupon(userId, couponId, issueRequestId);
            } catch (Exception e) {
                log.error("쿠폰 발급 이벤트 처리 실패. record={}", record, e);
            }
        }
        acknowledgment.acknowledge();
    }
}
