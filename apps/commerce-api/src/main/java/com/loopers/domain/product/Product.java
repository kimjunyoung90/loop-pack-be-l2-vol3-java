package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
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

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Brand brand;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Builder
    private Product(Brand brand, String name, int price, int stock) {
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void update(Brand brand, String name, int price, int stock) {
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
