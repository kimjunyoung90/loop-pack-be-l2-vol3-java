package com.loopers.application.product.command;

public record CreateProductCommand(
        Long brandId,
        String name,
        int price,
        int stock
) {
}
