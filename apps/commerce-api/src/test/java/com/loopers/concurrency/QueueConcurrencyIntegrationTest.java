package com.loopers.concurrency;

import com.loopers.application.queue.QueueService;
import com.loopers.application.queue.result.QueueTokenResult;
import com.loopers.infrastructure.queue.QueueScheduler;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
class QueueConcurrencyIntegrationTest {

    @Autowired
    private QueueService queueService;

    @MockitoBean
    private QueueScheduler queueScheduler;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @Test
    void 동시에_100명이_대기열에_진입하면_순서가_정확히_보장된다() {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<QueueTokenResult> results = new CopyOnWriteArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    QueueTokenResult result = queueService.enterQueue(userId);
                    results.add(result);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        // 모든 유저의 position은 1~100 범위 내 유니크한 값
        List<Long> positions = results.stream()
                .map(QueueTokenResult::position)
                .sorted()
                .toList();
        assertThat(positions).hasSize(threadCount);
        assertThat(positions.getFirst()).isEqualTo(1L);
        assertThat(positions.getLast()).isEqualTo((long) threadCount);
    }

    @Test
    void 같은_유저가_동시에_대기열에_진입해도_중복_진입되지_않는다() {
        // given
        int threadCount = 10;
        Long userId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    queueService.enterQueue(userId);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        // 동일 유저이므로 position은 항상 1
        QueueTokenResult result = queueService.enterQueue(userId);
        assertThat(result.position()).isEqualTo(1L);
    }
}
