package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Column(nullable = false)
    private Long brandId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Builder
    private Product(Long brandId, String name, int price, int stock) {
        this.brandId = brandId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void update(Long brandId, String name, int price, int stock) {
        this.brandId = brandId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
