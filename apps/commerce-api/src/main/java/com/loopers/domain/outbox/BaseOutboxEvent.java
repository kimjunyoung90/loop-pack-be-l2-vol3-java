package com.loopers.domain.outbox;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseOutboxEvent extends BaseEntity {

    protected static final int MAX_RETRY_COUNT = 3;
    protected static final String DLT_SUFFIX = ".DLT";

    @Column(name = "event_id", nullable = false, unique = true)
    protected String eventId;

    @Column(name = "topic", nullable = false)
    protected String topic;

    @Column(name = "message_key", nullable = false)
    protected String messageKey;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    protected String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    protected OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    protected int retryCount;

    protected BaseOutboxEvent(String topic, String messageKey, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void recordFailure() {
        this.retryCount++;
        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = OutboxStatus.FAILED;
        }
    }

    public boolean isPending() {
        return this.status == OutboxStatus.PENDING;
    }

    public boolean isFailed() {
        return this.status == OutboxStatus.FAILED;
    }

    public boolean isDeadLetter() {
        return this.topic.endsWith(DLT_SUFFIX);
    }

    public abstract BaseOutboxEvent createDeadLetterEvent();

    public abstract OutboxDomain getDomain();

    @Override
    protected void guard() {
        if (topic == null || topic.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "topic은 필수입니다.");
        }
        if (messageKey == null || messageKey.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "messageKey는 필수입니다.");
        }
        if (payload == null || payload.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "payload는 필수입니다.");
        }
    }
}
