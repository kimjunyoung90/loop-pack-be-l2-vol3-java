package com.loopers.domain.outbox;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    private static final int MAX_RETRY_COUNT = 3;
    private static final String DLT_SUFFIX = ".DLT";

    @Builder
    private OutboxEvent(String topic, String messageKey, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        guard();
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

    public boolean isFailed() {
        return this.status == OutboxStatus.FAILED;
    }

    public boolean isDeadLetter() {
        return this.topic.endsWith(DLT_SUFFIX);
    }

    public OutboxEvent createDeadLetterEvent() {
        return OutboxEvent.builder()
                .topic(this.topic + DLT_SUFFIX)
                .messageKey(this.messageKey)
                .payload(this.payload)
                .build();
    }

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
