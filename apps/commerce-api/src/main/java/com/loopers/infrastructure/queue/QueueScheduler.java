package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueScheduler {

    private final QueueRepository queueRepository;
    private final QueueProperties queueProperties;

    @Scheduled(fixedDelayString = "${queue.scheduler.interval-ms}")
    public void processQueue() {
        int batchSize = queueProperties.scheduler().batchSize();
        List<Long> userIds = queueRepository.popFromQueue(batchSize);

        for (Long userId : userIds) {
            String token = UUID.randomUUID().toString();
            queueRepository.issueToken(userId, token);
        }

        if (!userIds.isEmpty()) {
            log.info("대기열에서 {}명에게 입장 토큰을 발급했습니다.", userIds.size());
        }
    }
}
