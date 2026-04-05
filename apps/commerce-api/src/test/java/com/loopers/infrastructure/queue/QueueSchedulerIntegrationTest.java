package com.loopers.infrastructure.queue;

import com.loopers.application.queue.QueueService;
import com.loopers.application.queue.result.QueuePositionResult;
import com.loopers.domain.queue.QueueRepository;
import com.loopers.domain.queue.QueueToken;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
class QueueSchedulerIntegrationTest {

    @Autowired
    private QueueScheduler queueScheduler;

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @Test
    void 스케줄러가_배치_크기만큼_토큰을_발급한다() {
        // given — 배치 크기(10)보다 많은 20명을 대기열에 넣는다
        for (long i = 1; i <= 20; i++) {
            queueRepository.enqueue(new QueueToken(i, System.currentTimeMillis() + i));
        }

        // when — 스케줄러 1회 실행
        queueScheduler.processQueue();

        // then — 앞쪽 10명은 ALLOWED, 나머지 10명은 WAITING
        int allowedCount = 0;
        int waitingCount = 0;
        for (long i = 1; i <= 20; i++) {
            QueuePositionResult result = queueService.getQueuePosition(i);
            if ("ALLOWED".equals(result.status())) {
                allowedCount++;
            } else if ("WAITING".equals(result.status())) {
                waitingCount++;
            }
        }
        assertThat(allowedCount).isEqualTo(10);
        assertThat(waitingCount).isEqualTo(10);
    }

    @Test
    void 스케줄러를_여러번_실행하면_전체_대기열이_처리된다() {
        // given — 25명을 대기열에 넣는다
        for (long i = 1; i <= 25; i++) {
            queueRepository.enqueue(new QueueToken(i, System.currentTimeMillis() + i));
        }

        // when — 스케줄러 3회 실행 (10 + 10 + 5)
        queueScheduler.processQueue();
        queueScheduler.processQueue();
        queueScheduler.processQueue();

        // then — 전원 ALLOWED
        for (long i = 1; i <= 25; i++) {
            QueuePositionResult result = queueService.getQueuePosition(i);
            assertThat(result.status()).isEqualTo("ALLOWED");
        }
    }

    @Test
    void 대기열이_비어있으면_스케줄러_실행해도_오류가_발생하지_않는다() {
        // when & then
        queueScheduler.processQueue();
        // 예외 없이 정상 종료
    }

    @Test
    void 배치_크기보다_적은_인원이_대기열에_있으면_있는_인원만_처리한다() {
        // given — 3명만 대기열에 넣는다 (배치 크기 10보다 적음)
        for (long i = 1; i <= 3; i++) {
            queueRepository.enqueue(new QueueToken(i, System.currentTimeMillis() + i));
        }

        // when
        queueScheduler.processQueue();

        // then — 3명 모두 ALLOWED
        for (long i = 1; i <= 3; i++) {
            QueuePositionResult result = queueService.getQueuePosition(i);
            assertThat(result.status()).isEqualTo("ALLOWED");
        }
        assertThat(queueRepository.getWaitingCount()).isEqualTo(0);
    }
}
