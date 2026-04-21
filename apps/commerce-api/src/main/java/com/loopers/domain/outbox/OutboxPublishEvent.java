package com.loopers.domain.outbox;

public record OutboxPublishEvent(Long outboxEventId, OutboxDomain domain) {
}
