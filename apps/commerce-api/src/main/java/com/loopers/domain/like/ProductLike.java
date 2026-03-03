package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    public ProductLike(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
}
