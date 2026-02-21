package com.loopers.application.product;

public record UpdateProductCommand(
        Long brandId,
        String name,
        int price,
        int stock
) {
}
