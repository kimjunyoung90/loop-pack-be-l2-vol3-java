package com.loopers.application.outbox;

import com.loopers.domain.outbox.CouponOutboxEvent;
import com.loopers.domain.outbox.CouponOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponOutboxEventService {

    private final CouponOutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAndPublish(String topic, String messageKey, String payload) {
        outboxEventRepository.save(CouponOutboxEvent.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .build());
    }
}
