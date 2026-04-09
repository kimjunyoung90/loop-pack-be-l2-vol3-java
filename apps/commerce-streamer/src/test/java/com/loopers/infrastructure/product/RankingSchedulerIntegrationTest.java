package com.loopers.infrastructure.product;

import com.loopers.config.redis.RedisConfig;
import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.domain.product.ProductRankingRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
@Transactional
class RankingSchedulerIntegrationTest {

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    @Autowired
    private ProductRankingRepository productRankingRepository;

    @Autowired
    @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 일간_랭킹_집계_시_DB_메트릭이_Redis에_적재된다() {
        // given
        LocalDate metricDate = LocalDate.now();
        LocalDate targetDate = metricDate.plusDays(1);
        String key = "ranking:all:" + targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        productMetricsRepository.upsertViewCount(1L, metricDate, 10);
        productMetricsRepository.upsertLikeCount(1L, metricDate, 5);
        productMetricsRepository.upsertSalesCount(1L, metricDate, 3);

        productMetricsRepository.upsertViewCount(2L, metricDate, 20);
        productMetricsRepository.upsertLikeCount(2L, metricDate, 2);
        productMetricsRepository.upsertSalesCount(2L, metricDate, 1);

        entityManager.flush();
        entityManager.clear();

        // when — 스케줄러와 동일한 로직: DB 메트릭 조회 → score 계산 → Redis 적재
        var metricsList = productMetricsRepository.findAllByMetricDate(metricDate);
        var scores = new java.util.HashMap<Long, Double>();
        for (var metrics : metricsList) {
            double score = (metrics.getViewCount() * 0.1)
                    + (metrics.getLikeCount() * 0.2)
                    + (metrics.getSalesCount() * 0.6);
            scores.put(metrics.getProductId(), score);
        }
        productRankingRepository.incrementScores(targetDate, scores);

        // then
        Double score1 = redisTemplate.opsForZSet().score(key, "1");
        Double score2 = redisTemplate.opsForZSet().score(key, "2");

        // 상품1: view(10*0.1) + like(5*0.2) + sales(3*0.6) = 1.0 + 1.0 + 1.8 = 3.8
        // 상품2: view(20*0.1) + like(2*0.2) + sales(1*0.6) = 2.0 + 0.4 + 0.6 = 3.0
        assertThat(score1).isNotNull();
        assertThat(score2).isNotNull();
        assertThat(score1).isGreaterThan(score2);
    }
}
