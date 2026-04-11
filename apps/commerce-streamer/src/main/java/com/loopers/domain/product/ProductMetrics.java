package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "product_metrics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "metric_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetrics extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "sales_count", nullable = false)
    private int salesCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Builder
    private ProductMetrics(Long productId, LocalDate metricDate) {
        this.productId = productId;
        this.metricDate = metricDate;
        this.likeCount = 0;
        this.salesCount = 0;
        this.viewCount = 0;
        guard();
    }

    @Override
    protected void guard() {
        if (productId == null) {
            throw new IllegalArgumentException("productId는 필수입니다.");
        }
        if (metricDate == null) {
            throw new IllegalArgumentException("metricDate는 필수입니다.");
        }
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSalesCount(int quantity) {
        this.salesCount += quantity;
    }
}
