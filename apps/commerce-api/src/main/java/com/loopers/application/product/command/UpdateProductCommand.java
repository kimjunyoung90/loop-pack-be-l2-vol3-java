package com.loopers.application.product.command;

public record UpdateProductCommand(
        Long brandId,
        String name,
        int price,
        int stock
) {
}
