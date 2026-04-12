package com.loopers.infrastructure.queue;

import com.loopers.application.queue.QueueService;
import com.loopers.application.queue.result.QueuePositionResult;
import com.loopers.domain.queue.QueueRepository;
import com.loopers.domain.queue.QueueToken;
import com.loopers.support.error.CoreException;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
class QueueTokenIntegrationTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @Test
    void 토큰이_발급되면_ALLOWED_상태로_조회된다() {
        // given
        Long userId = 1L;
        queueRepository.enqueue(new QueueToken(userId, System.currentTimeMillis()));

        List<String> tokens = List.of(UUID.randomUUID().toString());
        queueRepository.popAndIssueTokens(1, tokens, List.of(System.currentTimeMillis()));

        // when
        QueuePositionResult result = queueService.getQueuePosition(userId);

        // then
        assertThat(result.status()).isEqualTo("ALLOWED");
        assertThat(result.token()).isNotNull();
    }

    @Test
    void 유효한_토큰으로_검증하면_예외가_발생하지_않는다() {
        // given
        Long userId = 1L;
        queueRepository.enqueue(new QueueToken(userId, System.currentTimeMillis()));

        String token = UUID.randomUUID().toString();
        queueRepository.popAndIssueTokens(1, List.of(token), List.of(System.currentTimeMillis()));

        // when & then
        queueService.validateToken(token, userId);
    }

    @Test
    void 유효하지_않은_토큰으로_검증하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        queueRepository.enqueue(new QueueToken(userId, System.currentTimeMillis()));

        List<String> tokens = List.of(UUID.randomUUID().toString());
        queueRepository.popAndIssueTokens(1, tokens, List.of(System.currentTimeMillis()));

        // when & then
        assertThatThrownBy(() -> queueService.validateToken("invalid-token", userId))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 토큰_삭제_후_조회하면_NOT_FOUND_상태가_반환된다() {
        // given
        Long userId = 1L;
        queueRepository.enqueue(new QueueToken(userId, System.currentTimeMillis()));

        List<String> tokens = List.of(UUID.randomUUID().toString());
        queueRepository.popAndIssueTokens(1, tokens, List.of(System.currentTimeMillis()));

        // when
        queueService.removeToken(userId);
        QueuePositionResult result = queueService.getQueuePosition(userId);

        // then
        assertThat(result.status()).isEqualTo("NOT_FOUND");
    }

    @Test
    void TTL이_만료된_토큰으로_검증하면_예외가_발생한다() throws InterruptedException {
        // given — 직접 Redis에 짧은 TTL(1초)로 토큰 저장
        Long userId = 1L;
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("entry-token:" + userId, token, Duration.ofSeconds(1));

        // TTL 만료 대기
        Thread.sleep(1500);

        // when & then
        assertThatThrownBy(() -> queueService.validateToken(token, userId))
                .isInstanceOf(CoreException.class);
    }
}
