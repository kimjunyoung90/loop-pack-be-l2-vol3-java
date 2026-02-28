package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Builder
    private Product(Long brandId, String name, int price, int stock) {
        if (price <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 가격은 0보다 커야 합니다.");
        }
        if (stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 재고는 0 이상이어야 합니다.");
        }
        this.brandId = brandId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void update(Long brandId, String name, int price, int stock) {
        if (price <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 가격은 0보다 커야 합니다.");
        }
        if (stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 재고는 0 이상이어야 합니다.");
        }
        this.brandId = brandId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void deductStock(int quantity) {
        if (this.stock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다. 현재 재고: " + this.stock);
        }
        this.stock -= quantity;
    }

    public void restoreStock(int quantity) {
        this.stock += quantity;
    }
}
