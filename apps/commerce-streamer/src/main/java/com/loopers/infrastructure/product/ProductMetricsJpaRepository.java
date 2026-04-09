package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {

    Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, LocalDate metricDate);

    List<ProductMetrics> findAllByMetricDate(LocalDate metricDate);

    @Modifying
    @Query(value = """
            INSERT INTO product_metrics (product_id, metric_date, like_count, sales_count, view_count, created_at, updated_at)
            VALUES (:productId, :metricDate, :delta, 0, 0, NOW(), NOW())
            ON DUPLICATE KEY UPDATE like_count = like_count + :delta, updated_at = NOW()
            """, nativeQuery = true)
    void upsertLikeCount(Long productId, LocalDate metricDate, int delta);

    @Modifying
    @Query(value = """
            INSERT INTO product_metrics (product_id, metric_date, like_count, sales_count, view_count, created_at, updated_at)
            VALUES (:productId, :metricDate, 0, 0, :delta, NOW(), NOW())
            ON DUPLICATE KEY UPDATE view_count = view_count + :delta, updated_at = NOW()
            """, nativeQuery = true)
    void upsertViewCount(Long productId, LocalDate metricDate, int delta);

    @Modifying
    @Query(value = """
            INSERT INTO product_metrics (product_id, metric_date, like_count, sales_count, view_count, created_at, updated_at)
            VALUES (:productId, :metricDate, 0, :delta, 0, NOW(), NOW())
            ON DUPLICATE KEY UPDATE sales_count = sales_count + :delta, updated_at = NOW()
            """, nativeQuery = true)
    void upsertSalesCount(Long productId, LocalDate metricDate, int delta);
}
