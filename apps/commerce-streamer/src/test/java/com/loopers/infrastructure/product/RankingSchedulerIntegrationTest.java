package com.loopers.infrastructure.product;

import com.loopers.config.redis.RedisConfig;
import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
@Transactional
class RankingSchedulerIntegrationTest {

    @Autowired
    private RankingScheduler rankingScheduler;

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    @Autowired
    @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 일간_랭킹_집계_시_DB_메트릭이_Redis에_적재된다() {
        // given
        LocalDate today = LocalDate.now();
        productMetricsRepository.upsertViewCount(1L, today, 10);
        productMetricsRepository.upsertLikeCount(1L, today, 5);
        productMetricsRepository.upsertSalesCount(1L, today, 3);

        productMetricsRepository.upsertViewCount(2L, today, 20);
        productMetricsRepository.upsertLikeCount(2L, today, 2);
        productMetricsRepository.upsertSalesCount(2L, today, 1);

        entityManager.flush();
        entityManager.clear();

        // when
        rankingScheduler.aggregate(today);

        // then
        String key = "ranking:all:" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);

        assertThat(tuples).isNotEmpty();

        // 상품1, 상품2의 score가 Redis에 적재되었는지 확인
        Double score1 = redisTemplate.opsForZSet().score(key, "1");
        Double score2 = redisTemplate.opsForZSet().score(key, "2");

        // 상품1: view(10*0.1) + like(5*0.2) + sales(3*0.6) = 1.0 + 1.0 + 1.8 = 3.8
        // 상품2: view(20*0.1) + like(2*0.2) + sales(1*0.6) = 2.0 + 0.4 + 0.6 = 3.0
        assertThat(score1).isNotNull();
        assertThat(score2).isNotNull();
        assertThat(score1).isGreaterThan(score2);
    }
}
