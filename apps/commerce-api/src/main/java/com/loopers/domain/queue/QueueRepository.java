package com.loopers.domain.queue;

import java.util.List;
import java.util.Optional;

public interface QueueRepository {

    boolean enqueue(QueueToken token);

    Optional<String> findTokenByUserId(Long userId);

    Long getRank(Long token);

    List<Long> popAndIssueTokens(int count, List<String> tokens, List<Long> orderableAts);

    Optional<Long> findOrderableAtByUserId(Long userId);

    void removeToken(Long userId);

    long getWaitingCount();
}
