package com.loopers.application.outbox;

import com.loopers.domain.outbox.OrderOutboxEvent;
import com.loopers.domain.outbox.OrderOutboxEventRepository;
import com.loopers.domain.outbox.OutboxDomain;
import com.loopers.domain.outbox.OutboxPublishEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OrderOutboxEventService {

    private final OrderOutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAndPublish(String topic, String messageKey, String payload) {
        OrderOutboxEvent saved = outboxEventRepository.save(OrderOutboxEvent.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .build());
        eventPublisher.publishEvent(new OutboxPublishEvent(saved.getId(), OutboxDomain.ORDER));
    }
}
