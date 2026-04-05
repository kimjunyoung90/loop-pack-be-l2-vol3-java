package com.loopers.domain.queue;

import java.util.List;
import java.util.Optional;

public interface QueueRepository {

    void enqueue(QueueToken token);

    Optional<String> findTokenByUserId(Long userId);

    Long getRank(Long token);

    List<Long> popAndIssueTokens(int count, List<String> tokens, List<Long> orderableAts);

    Optional<Long> findOrderableAtByUserId(Long userId);

    void removeToken(Long userId);

    boolean isAlreadyQueued(Long userId);

    long getWaitingCount();
}
