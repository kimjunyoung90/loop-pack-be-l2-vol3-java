package com.loopers.application.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
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

    @Test
    void 좋아요_증가_시_오늘_날짜로_메트릭이_적재된다() {
        // given
        Long productId = 1L;

        // when
        productMetricsService.incrementLikeCount(productId);

        // then
        ProductMetrics metrics = productMetricsRepository.findByProductIdAndMetricDate(productId, LocalDate.now())
                .orElseThrow();
        assertThat(metrics.getMetricDate()).isEqualTo(LocalDate.now());
        assertThat(metrics.getLikeCount()).isEqualTo(1);
        assertThat(metrics.getViewCount()).isEqualTo(0);
        assertThat(metrics.getSalesCount()).isEqualTo(0);
    }

    @Test
    void 같은_날_같은_상품에_여러_메트릭이_하나의_row에_누적된다() {
        // given
        Long productId = 1L;

        // when
        productMetricsService.incrementLikeCount(productId);
        productMetricsService.incrementViewCount(productId);
        productMetricsService.incrementSalesCount(productId, 3);

        // then
        ProductMetrics metrics = productMetricsRepository.findByProductIdAndMetricDate(productId, LocalDate.now())
                .orElseThrow();
        assertThat(metrics.getLikeCount()).isEqualTo(1);
        assertThat(metrics.getViewCount()).isEqualTo(1);
        assertThat(metrics.getSalesCount()).isEqualTo(3);
    }
}
