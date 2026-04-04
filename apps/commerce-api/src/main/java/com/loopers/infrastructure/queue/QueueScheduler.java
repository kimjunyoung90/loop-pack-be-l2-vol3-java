package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueScheduler {

    private final QueueRepository queueRepository;
    private final QueueProperties queueProperties;

    @Scheduled(fixedDelayString = "${queue.scheduler.interval-ms}")
    public void processQueue() {
        int batchSize = queueProperties.scheduler().batchSize();
        queueRepository.popAndAllow(batchSize);

	}
}
