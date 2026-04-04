package com.loopers.domain.queue;

import java.util.Optional;

public interface QueueRepository {

    void enqueue(QueueToken token);

    Optional<String> findTokenByUserId(Long userId);

    Long getRank(Long token);

    void popAndAllow(int count);

    void removeToken(Long userId);

    boolean isAlreadyQueued(Long userId);

    long getWaitingCount();
}
