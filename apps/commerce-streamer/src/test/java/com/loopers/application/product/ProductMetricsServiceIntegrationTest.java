package com.loopers.application.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
@Transactional
class ProductMetricsServiceIntegrationTest {

    @Autowired
    private ProductMetricsService productMetricsService;

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 좋아요_증가_시_지정된_날짜로_메트릭이_적재된다() {
        // given
        Long productId = 1L;
        LocalDate eventDate = LocalDate.of(2026, 4, 9);

        // when
        productMetricsService.incrementLikeCount(productId, eventDate);

        entityManager.flush();
        entityManager.clear();

        // then
        ProductMetrics metrics = productMetricsRepository.findByProductIdAndMetricDate(productId, eventDate)
                .orElseThrow();
        assertThat(metrics.getMetricDate()).isEqualTo(eventDate);
        assertThat(metrics.getLikeCount()).isEqualTo(1);
        assertThat(metrics.getViewCount()).isEqualTo(0);
        assertThat(metrics.getSalesCount()).isEqualTo(0);
    }

    @Test
    void 같은_날_같은_상품에_여러_메트릭이_하나의_row에_누적된다() {
        // given
        Long productId = 1L;
        LocalDate eventDate = LocalDate.of(2026, 4, 9);

        // when
        productMetricsService.incrementLikeCount(productId, eventDate);
        productMetricsService.incrementViewCount(productId, eventDate);
        productMetricsService.incrementSalesCount(productId, 3, eventDate);

        entityManager.flush();
        entityManager.clear();

        // then
        ProductMetrics metrics = productMetricsRepository.findByProductIdAndMetricDate(productId, eventDate)
                .orElseThrow();
        assertThat(metrics.getLikeCount()).isEqualTo(1);
        assertThat(metrics.getViewCount()).isEqualTo(1);
        assertThat(metrics.getSalesCount()).isEqualTo(3);
    }

    @Test
    void 다른_날짜의_이벤트는_별도의_row에_적재된다() {
        // given
        Long productId = 1L;
        LocalDate day1 = LocalDate.of(2026, 4, 9);
        LocalDate day2 = LocalDate.of(2026, 4, 10);

        // when
        productMetricsService.incrementViewCount(productId, day1);
        productMetricsService.incrementViewCount(productId, day1);
        productMetricsService.incrementViewCount(productId, day2);

        entityManager.flush();
        entityManager.clear();

        // then
        ProductMetrics metricsDay1 = productMetricsRepository.findByProductIdAndMetricDate(productId, day1)
                .orElseThrow();
        ProductMetrics metricsDay2 = productMetricsRepository.findByProductIdAndMetricDate(productId, day2)
                .orElseThrow();

        assertThat(metricsDay1.getViewCount()).isEqualTo(2);
        assertThat(metricsDay2.getViewCount()).isEqualTo(1);
    }
}
