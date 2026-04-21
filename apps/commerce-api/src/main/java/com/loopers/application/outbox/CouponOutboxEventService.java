package com.loopers.application.outbox;

import com.loopers.domain.outbox.CouponOutboxEvent;
import com.loopers.domain.outbox.CouponOutboxEventRepository;
import com.loopers.domain.outbox.OutboxDomain;
import com.loopers.domain.outbox.OutboxPublishEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponOutboxEventService {

    private final CouponOutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAndPublish(String topic, String messageKey, String payload) {
        CouponOutboxEvent saved = outboxEventRepository.save(CouponOutboxEvent.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .build());
        eventPublisher.publishEvent(new OutboxPublishEvent(saved.getId(), OutboxDomain.COUPON));
    }
}
