package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponIssueRequest;
import com.loopers.domain.coupon.CouponIssueRequestRepository;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class CouponIssueProcessor {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponIssueRequestRepository couponIssueRequestRepository;
    private final EventHandledRepository eventHandledRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(ConsumerRecord<Object, Object> record) throws Exception {
        String eventId = extractEventId(record);
        if (eventId != null && eventHandledRepository.existsByEventId(eventId)) {
            return;
        }

        JsonNode node = objectMapper.readTree(record.value().toString());
        Long userId = node.get("userId").asLong();
        Long couponId = node.get("couponId").asLong();
        Long issueRequestId = node.get("issueRequestId").asLong();

        CouponIssueRequest issueRequest = couponIssueRequestRepository.findByIdAndDeletedAtIsNull(issueRequestId)
                .orElse(null);
        if (issueRequest == null || !issueRequest.isPending()) {
            markHandled(eventId);
            return;
        }

        if (userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(userId, couponId)) {
            issueRequest.approve();
            markHandled(eventId);
            return;
        }

        try {
            Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(couponId)
                    .orElseThrow(() -> new IllegalStateException("쿠폰을 찾을 수 없습니다."));

            UserCoupon userCoupon = coupon.issue(userId);
            userCouponRepository.save(userCoupon);
            issueRequest.approve();
        } catch (IllegalStateException e) {
            issueRequest.reject(e.getMessage());
        }

        markHandled(eventId);
    }

    private void markHandled(String eventId) {
        if (eventId != null) {
            eventHandledRepository.save(EventHandled.builder().eventId(eventId).build());
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
