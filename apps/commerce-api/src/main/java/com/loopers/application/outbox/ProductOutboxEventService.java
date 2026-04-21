package com.loopers.application.outbox;

import com.loopers.domain.outbox.OutboxDomain;
import com.loopers.domain.outbox.OutboxPublishEvent;
import com.loopers.domain.outbox.ProductOutboxEvent;
import com.loopers.domain.outbox.ProductOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductOutboxEventService {

    private final ProductOutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAndPublish(String topic, String messageKey, String payload) {
        ProductOutboxEvent saved = outboxEventRepository.save(ProductOutboxEvent.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .build());
        eventPublisher.publishEvent(new OutboxPublishEvent(saved.getId(), OutboxDomain.PRODUCT));
    }
}
