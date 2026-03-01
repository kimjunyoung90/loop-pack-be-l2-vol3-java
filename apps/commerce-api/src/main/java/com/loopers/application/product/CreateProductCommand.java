package com.loopers.application.product;

public record CreateProductCommand(
        Long brandId,
        String name,
        int price,
        int stock
) {
}
