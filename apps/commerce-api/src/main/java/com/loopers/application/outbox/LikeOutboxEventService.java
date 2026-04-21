package com.loopers.application.outbox;

import com.loopers.domain.outbox.LikeOutboxEvent;
import com.loopers.domain.outbox.LikeOutboxEventRepository;
import com.loopers.domain.outbox.OutboxDomain;
import com.loopers.domain.outbox.OutboxPublishEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeOutboxEventService {

    private final LikeOutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveAndPublish(String topic, String messageKey, String payload) {
        LikeOutboxEvent saved = outboxEventRepository.save(LikeOutboxEvent.builder()
                .topic(topic)
                .messageKey(messageKey)
                .payload(payload)
                .build());
        eventPublisher.publishEvent(new OutboxPublishEvent(saved.getId(), OutboxDomain.LIKE));
    }
}
