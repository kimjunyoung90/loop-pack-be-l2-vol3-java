package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueScheduler {

    private final QueueRepository queueRepository;
    private final QueueProperties queueProperties;

	//주기적으로 대기큐에서 꺼내서 입장 토큰을 발급 (Lua 스크립트로 원자적 처리)
    @Scheduled(fixedDelayString = "${queue.scheduler.interval-ms}")
    public void processQueue() {
        int batchSize = queueProperties.scheduler().batchSize();

        List<String> tokens = IntStream.range(0, batchSize)
                .mapToObj(i -> UUID.randomUUID().toString())
                .toList();

        // 유저별 주문 가능 시각 계산 (Thundering Herd 완화)
        long now = System.currentTimeMillis();
        long spreadIntervalMs = queueProperties.scheduler().spreadIntervalMs();
        long jitterMaxMs = queueProperties.scheduler().jitterMaxMs();
        List<Long> orderableAts = IntStream.range(0, batchSize)
                .mapToLong(i -> now + (spreadIntervalMs * i)
                        + (jitterMaxMs > 0 ? ThreadLocalRandom.current().nextLong(jitterMaxMs) : 0))
                .boxed()
                .toList();

        List<Long> issuedUserIds = queueRepository.popAndIssueTokens(batchSize, tokens, orderableAts);

        if (!issuedUserIds.isEmpty()) {
            log.info("대기열에서 {}명에게 입장 토큰을 발급했습니다.", issuedUserIds.size());
        }
    }
}
