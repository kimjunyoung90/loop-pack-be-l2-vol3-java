package com.loopers.domain.outbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutboxEvent extends BaseOutboxEvent {

    @Builder
    private OrderOutboxEvent(String topic, String messageKey, String payload) {
        super(topic, messageKey, payload);
        guard();
    }

    @Override
    public OrderOutboxEvent createDeadLetterEvent() {
        return OrderOutboxEvent.builder()
                .topic(this.topic + DLT_SUFFIX)
                .messageKey(this.messageKey)
                .payload(this.payload)
                .build();
    }

    @Override
    public OutboxDomain getDomain() {
        return OutboxDomain.ORDER;
    }
}
