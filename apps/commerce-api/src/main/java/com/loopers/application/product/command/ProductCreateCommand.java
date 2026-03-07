package com.loopers.application.product.command;

public record ProductCreateCommand(
        Long brandId,
        String name,
        int price,
        int stock
) {
}
