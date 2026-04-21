package com.loopers.domain.outbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOutboxEvent extends BaseOutboxEvent {

    @Builder
    private ProductOutboxEvent(String topic, String messageKey, String payload) {
        super(topic, messageKey, payload);
        guard();
    }

    @Override
    public ProductOutboxEvent createDeadLetterEvent() {
        return ProductOutboxEvent.builder()
                .topic(this.topic + DLT_SUFFIX)
                .messageKey(this.messageKey)
                .payload(this.payload)
                .build();
    }

    @Override
    public OutboxDomain getDomain() {
        return OutboxDomain.PRODUCT;
    }
}
